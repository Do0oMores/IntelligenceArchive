package top.mores.intelligencearchive.client;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import top.mores.intelligencearchive.client.render.ArchiveNodeRenderContext;
import top.mores.intelligencearchive.client.render.ArchiveNodeRendererRegistry;
import top.mores.intelligencearchive.client.state.ArchiveClientArchiveState;
import top.mores.intelligencearchive.client.state.ResolvedContentLoadStatus;
import top.mores.intelligencearchive.client.view.ArchiveViewModel;
import top.mores.intelligencearchive.client.view.ArchiveViewModelMapper;
import top.mores.intelligencearchive.client.view.ArchiveViewNode;
import top.mores.intelligencearchive.client.view.AudioViewNode;
import top.mores.intelligencearchive.client.view.ImageViewNode;
import top.mores.intelligencearchive.client.view.IntelLinkViewNode;
import top.mores.intelligencearchive.client.view.RedactedViewNode;
import top.mores.intelligencearchive.client.view.TextViewNode;
import top.mores.intelligencearchive.common.content.resolution.ResolvedArchiveContent;
import top.mores.intelligencearchive.common.content.resolution.ResolvedAudioNode;
import top.mores.intelligencearchive.common.content.resolution.ResolvedImageNode;
import top.mores.intelligencearchive.common.content.resolution.ResolvedIntelLinkNode;
import top.mores.intelligencearchive.common.content.resolution.ResolvedRedactedNode;
import top.mores.intelligencearchive.common.content.resolution.ResolvedRedactionState;
import top.mores.intelligencearchive.common.content.resolution.ResolvedTextNode;
import top.mores.intelligencearchive.common.dto.ResolvedArchiveContentDTO;
import top.mores.intelligencearchive.common.dto.ResolvedRedactedNodeDTO;
import top.mores.intelligencearchive.network.packet.RequestResolvedArchiveContentPacket;
import top.mores.intelligencearchive.network.packet.ResponseResolvedArchiveContentPacket;
import top.mores.intelligencearchive.server.mapper.ResolvedArchiveContentMapper;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContextBuilder;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Phase 3-C-2 DTO、ViewModel、Renderer 与客户端状态回归测试。 */
class ArchiveClientRendererTest {
    @Test
    void resolvedDomainMapsToBoundedNetworkDtoWithoutConditionReference() {
        ResolvedArchiveContentDTO dto = ResolvedArchiveContentMapper.toDto(sampleResolvedContent());

        assertEquals("document.case.test_001", dto.documentId());
        assertEquals(5, dto.nodes().size());
        ResolvedRedactedNodeDTO redacted = assertInstanceOf(
                ResolvedRedactedNodeDTO.class,
                dto.nodes().get(4)
        );
        assertEquals("[REDACTED]", redacted.placeholder());
        assertEquals(ResolvedRedactedNodeDTO.RedactionState.REDACTED, redacted.state());
        assertFalse(redacted.toString().contains("security.level3"));
    }

    @Test
    void resolvedContentPacketsRoundTripEverySupportedNode() {
        RequestResolvedArchiveContentPacket request = new RequestResolvedArchiveContentPacket(
                "document.case.test_001"
        );
        FriendlyByteBuf requestBuffer = new FriendlyByteBuf(Unpooled.buffer());
        FriendlyByteBuf responseBuffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            RequestResolvedArchiveContentPacket.encode(request, requestBuffer);
            assertEquals(request, RequestResolvedArchiveContentPacket.decode(requestBuffer));

            ResponseResolvedArchiveContentPacket response = ResponseResolvedArchiveContentPacket.success(
                    ResolvedArchiveContentMapper.toDto(sampleResolvedContent())
            );
            ResponseResolvedArchiveContentPacket.encode(response, responseBuffer);
            assertEquals(response, ResponseResolvedArchiveContentPacket.decode(responseBuffer));
        } finally {
            requestBuffer.release();
            responseBuffer.release();
        }
    }

    @Test
    void dtoConvertsToDisplayOnlyViewModel() {
        ArchiveViewModel viewModel = ArchiveViewModelMapper.fromDto(
                ResolvedArchiveContentMapper.toDto(sampleResolvedContent())
        );

        assertEquals("content.case.test_001.v1", viewModel.contentId());
        assertInstanceOf(TextViewNode.class, viewModel.nodes().get(0));
        assertInstanceOf(ImageViewNode.class, viewModel.nodes().get(1));
        assertInstanceOf(AudioViewNode.class, viewModel.nodes().get(2));
        assertInstanceOf(IntelLinkViewNode.class, viewModel.nodes().get(3));
        assertInstanceOf(RedactedViewNode.class, viewModel.nodes().get(4));
    }

    @Test
    void defaultRuntimeResolvesTheDevelopmentDocumentEndToEnd() {
        var result = new ArchiveRuntimeContextBuilder()
                .build()
                .getResolveArchiveContentUseCase()
                .execute(UUID.randomUUID(), "document.case.test_001");

        assertTrue(result.success());
        assertFalse(result.content().orElseThrow().nodes().isEmpty());
        assertTrue(result.content().orElseThrow().nodes().stream()
                .anyMatch(node -> node instanceof ResolvedTextNode));
    }

    @Test
    void rendererRegistryDispatchesAllNodeTypes() {
        ArchiveNodeRendererRegistry registry = ArchiveNodeRendererRegistry.createDefault();
        ArchiveViewModel viewModel = ArchiveViewModelMapper.fromDto(
                ResolvedArchiveContentMapper.toDto(sampleResolvedContent())
        );

        assertEquals("TextArchiveNodeRenderer", registry.rendererFor(viewModel.nodes().get(0)).getClass().getSimpleName());
        assertEquals("ImageArchiveNodeRenderer", registry.rendererFor(viewModel.nodes().get(1)).getClass().getSimpleName());
        assertEquals("AudioArchiveNodeRenderer", registry.rendererFor(viewModel.nodes().get(2)).getClass().getSimpleName());
        assertEquals("IntelLinkArchiveNodeRenderer", registry.rendererFor(viewModel.nodes().get(3)).getClass().getSimpleName());
        assertEquals("RedactedArchiveNodeRenderer", registry.rendererFor(viewModel.nodes().get(4)).getClass().getSimpleName());
    }

    @Test
    void textRendererDrawsVisibleTextWithoutParsingMarkdown() {
        FakeRenderContext context = new FakeRenderContext();
        ArchiveNodeRendererRegistry registry = ArchiveNodeRendererRegistry.createDefault();

        registry.render(context, new TextViewNode("**literal markdown**"), 1, 2, 120);

        assertEquals("**literal markdown**", context.lastText());
    }

    @Test
    void imageAndAudioRenderersRemainPlaceholders() {
        FakeRenderContext context = new FakeRenderContext();
        ArchiveNodeRendererRegistry registry = ArchiveNodeRendererRegistry.createDefault();

        registry.render(context, new ImageViewNode("intelligencearchive:textures/case.png"), 0, 0, 120);
        registry.render(context, new AudioViewNode("intelligencearchive:audio/interview_01"), 0, 0, 120);

        assertTrue(context.drawnTexts().get(0).startsWith("[IMAGE PLACEHOLDER]"));
        assertTrue(context.drawnTexts().get(1).startsWith("[AUDIO]"));
    }

    @Test
    void intelLinkRendererUsesClickHookWithoutUnlockingAnything() {
        ArchiveNodeRendererRegistry registry = ArchiveNodeRendererRegistry.createDefault();
        IntelLinkViewNode link = new IntelLinkViewNode("intel.person.witness");
        AtomicReference<String> clicked = new AtomicReference<>();

        assertTrue(registry.click(link, clicked::set));
        assertEquals("intel.person.witness", clicked.get());
    }

    @Test
    void redactedRendererDisplaysOnlyServerResult() {
        FakeRenderContext context = new FakeRenderContext();
        ArchiveNodeRendererRegistry registry = ArchiveNodeRendererRegistry.createDefault();

        registry.render(
                context,
                new RedactedViewNode("[REDACTED]", RedactedViewNode.State.REDACTED),
                0,
                0,
                120
        );

        assertEquals("[REDACTED]", context.lastText());
        assertFalse(context.lastText().contains("condition"));
    }

    @Test
    void clientStateTracksRequestLoadedFailedAndTimeoutStates() {
        ArchiveClientArchiveState state = new ArchiveClientArchiveState();
        ArchiveViewModel viewModel = ArchiveViewModelMapper.fromDto(
                ResolvedArchiveContentMapper.toDto(sampleResolvedContent())
        );

        assertEquals(ResolvedContentLoadStatus.IDLE, state.resolvedView().status());
        assertTrue(state.beginResolvedRequest(viewModel.documentId(), 100L));
        assertFalse(state.beginResolvedRequest(viewModel.documentId(), 101L));
        assertEquals(viewModel.documentId(), state.resolvedView().documentId());
        assertEquals(ResolvedContentLoadStatus.REQUESTING, state.resolvedView().status());
        state.acceptResolvedContent(viewModel.documentId(), viewModel);
        assertEquals(ResolvedContentLoadStatus.LOADED, state.resolvedView().status());

        assertTrue(state.beginResolvedRequest(viewModel.documentId(), 200L));
        state.tick(10_000_000_200L);
        assertEquals(ResolvedContentLoadStatus.FAILED, state.resolvedView().status());
        assertTrue(state.resolvedView().errorMessage().contains("10 seconds"));
    }

    @Test
    void viewModelContainsNoDtoResolutionOrServerTypes() {
        ArchiveViewModel viewModel = ArchiveViewModelMapper.fromDto(
                ResolvedArchiveContentMapper.toDto(sampleResolvedContent())
        );

        assertDisplayOnlyType(viewModel.getClass());
        for (ArchiveViewNode node : viewModel.nodes()) {
            assertDisplayOnlyType(node.getClass());
        }
    }

    @Test
    void dtoRejectsNodeCountsAboveProtocolLimit() {
        List<top.mores.intelligencearchive.common.dto.ResolvedContentNodeDTO> nodes = new ArrayList<>();
        for (int index = 0; index <= ResolvedArchiveContentDTO.MAX_NODES; index++) {
            nodes.add(new top.mores.intelligencearchive.common.dto.ResolvedTextNodeDTO("node-" + index));
        }

        assertThrows(IllegalArgumentException.class, () -> new ResolvedArchiveContentDTO(
                "document.case.test_001",
                "content.case.test_001.v1",
                "1",
                nodes
        ));
    }

    private static ResolvedArchiveContent sampleResolvedContent() {
        return new ResolvedArchiveContent(
                "document.case.test_001",
                "content.case.test_001.v1",
                "1",
                List.of(
                        new ResolvedTextNode("Server-resolved visible text."),
                        new ResolvedImageNode("intelligencearchive:textures/case.png"),
                        new ResolvedAudioNode("intelligencearchive:audio/interview_01"),
                        new ResolvedIntelLinkNode("intel.person.witness"),
                        new ResolvedRedactedNode("[REDACTED]", ResolvedRedactionState.REDACTED)
                )
        );
    }

    private static void assertDisplayOnlyType(Class<?> type) {
        assertTrue(type.getPackageName().startsWith("top.mores.intelligencearchive.client.view"));
        if (!type.isRecord()) {
            return;
        }
        for (RecordComponent component : type.getRecordComponents()) {
            String componentType = component.getType().getName();
            assertFalse(componentType.contains(".server."));
            assertFalse(componentType.contains(".common.dto."));
            assertFalse(componentType.contains(".content.resolution."));
        }
    }

    private static final class FakeRenderContext implements ArchiveNodeRenderContext {
        private final List<String> drawnTexts = new ArrayList<>();

        @Override
        public int lineHeight() {
            return 11;
        }

        @Override
        public int measureWrappedText(String text, int width) {
            return 11;
        }

        @Override
        public void drawWrappedText(String text, int x, int y, int width, int color) {
            drawnTexts.add(text);
        }

        private List<String> drawnTexts() {
            return List.copyOf(drawnTexts);
        }

        private String lastText() {
            return drawnTexts.get(drawnTexts.size() - 1);
        }
    }
}
