package top.mores.intelligencearchive.common.content.resolution;

import org.junit.jupiter.api.Test;
import top.mores.intelligencearchive.application.result.OperationStatus;
import top.mores.intelligencearchive.application.result.ResolveArchiveContentResult;
import top.mores.intelligencearchive.application.usecase.ResolveArchiveContentUseCase;
import top.mores.intelligencearchive.common.content.ArchiveContent;
import top.mores.intelligencearchive.common.content.AudioContentNode;
import top.mores.intelligencearchive.common.content.ContentNode;
import top.mores.intelligencearchive.common.content.ImageContentNode;
import top.mores.intelligencearchive.common.content.IntelLinkContentNode;
import top.mores.intelligencearchive.common.content.RedactedContentNode;
import top.mores.intelligencearchive.common.content.TextContentNode;
import top.mores.intelligencearchive.server.service.SimpleArchiveContentService;
import top.mores.intelligencearchive.server.service.SimpleIntelService;
import top.mores.intelligencearchive.server.service.SimpleInvestigationService;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Phase 3-C-1 玩家可见内容解析测试。 */
class ArchiveContentResolutionTest {
    private static final String CONDITION_ID = "security.level3";

    @Test
    void ordinaryTextIsAlwaysVisible() {
        ResolvedArchiveContent resolved = resolveForNewPlayer(contentWith(
                new TextContentNode("公开调查记录")
        ));

        ResolvedTextNode node = assertInstanceOf(ResolvedTextNode.class, resolved.nodes().get(0));
        assertEquals("公开调查记录", node.text());
    }

    @Test
    void imageReferenceIsPreservedWithoutLoading() {
        ResolvedArchiveContent resolved = resolveForNewPlayer(contentWith(
                new ImageContentNode("archive/image/lab.png")
        ));

        ResolvedImageNode node = assertInstanceOf(ResolvedImageNode.class, resolved.nodes().get(0));
        assertEquals("archive/image/lab.png", node.imageReference());
    }

    @Test
    void audioReferenceIsPreservedWithoutLoading() {
        ResolvedArchiveContent resolved = resolveForNewPlayer(contentWith(
                new AudioContentNode("archive/audio/radio.ogg")
        ));

        ResolvedAudioNode node = assertInstanceOf(ResolvedAudioNode.class, resolved.nodes().get(0));
        assertEquals("archive/audio/radio.ogg", node.audioReference());
    }

    @Test
    void intelLinkIsPreserved() {
        ResolvedArchiveContent resolved = resolveForNewPlayer(contentWith(
                new IntelLinkContentNode("node.location.lab")
        ));

        ResolvedIntelLinkNode node = assertInstanceOf(
                ResolvedIntelLinkNode.class,
                resolved.nodes().get(0)
        );
        assertEquals("node.location.lab", node.targetIntelId());
    }

    @Test
    void failedConditionProducesRedactedPlaceholder() {
        ResolvedArchiveContent resolved = resolveForNewPlayer(contentWith(
                new RedactedContentNode("██████", CONDITION_ID)
        ));

        ResolvedRedactedNode node = assertInstanceOf(
                ResolvedRedactedNode.class,
                resolved.nodes().get(0)
        );
        assertEquals("██████", node.placeholder());
        assertEquals(ResolvedRedactionState.REDACTED, node.state());
    }

    @Test
    void satisfiedConditionProducesSafeSatisfiedStateWithoutSecret() {
        UUID playerId = UUID.randomUUID();
        SimpleInvestigationService investigationService = investigationService();
        investigationService.discoverIntel(playerId, CONDITION_ID);
        ArchiveContent content = contentWith(new RedactedContentNode("██████", CONDITION_ID));

        ResolvedArchiveContent resolved = new ArchiveContentResolver().resolve(
                content,
                new PlayerContentContext(playerId, investigationService.getPlayerState(playerId))
        );

        ResolvedRedactedNode node = assertInstanceOf(
                ResolvedRedactedNode.class,
                resolved.nodes().get(0)
        );
        assertEquals(ResolvedRedactionState.CONDITION_SATISFIED, node.state());
        assertFalse(node.toString().contains(CONDITION_ID));
    }

    @Test
    void differentPlayersReceiveDifferentResolvedRedactionStates() {
        UUID clearedPlayer = UUID.randomUUID();
        UUID restrictedPlayer = UUID.randomUUID();
        SimpleInvestigationService investigationService = investigationService();
        investigationService.discoverIntel(clearedPlayer, CONDITION_ID);
        ArchiveContent content = contentWith(new RedactedContentNode("██████", CONDITION_ID));
        ArchiveContentResolver resolver = new ArchiveContentResolver();

        ResolvedRedactedNode cleared = assertInstanceOf(
                ResolvedRedactedNode.class,
                resolver.resolve(
                        content,
                        new PlayerContentContext(
                                clearedPlayer,
                                investigationService.getPlayerState(clearedPlayer)
                        )
                ).nodes().get(0)
        );
        ResolvedRedactedNode restricted = assertInstanceOf(
                ResolvedRedactedNode.class,
                resolver.resolve(
                        content,
                        new PlayerContentContext(
                                restrictedPlayer,
                                investigationService.getPlayerState(restrictedPlayer)
                        )
                ).nodes().get(0)
        );

        assertEquals(ResolvedRedactionState.CONDITION_SATISFIED, cleared.state());
        assertEquals(ResolvedRedactionState.REDACTED, restricted.state());
    }

    @Test
    void resolutionDoesNotModifyOriginalArchiveContent() {
        ArchiveContent original = contentWith(
                new TextContentNode("公开内容"),
                new RedactedContentNode("██████", CONDITION_ID)
        );
        List<ContentNode> originalNodes = original.nodes();

        ResolvedArchiveContent resolved = resolveForNewPlayer(original);

        assertSame(originalNodes, original.nodes());
        assertEquals(2, original.nodes().size());
        assertInstanceOf(RedactedContentNode.class, original.nodes().get(1));
        assertInstanceOf(ResolvedRedactedNode.class, resolved.nodes().get(1));
        assertThrows(UnsupportedOperationException.class,
                () -> resolved.nodes().add(new ResolvedTextNode("非法修改")));
    }

    @Test
    void resolverAndResolvedModelsDoNotDependOnMinecraftOrForge() {
        List<Class<?>> pureTypes = List.of(
                ArchiveContentResolver.class,
                PlayerContentContext.class,
                ResolvedArchiveContent.class,
                ResolvedTextNode.class,
                ResolvedImageNode.class,
                ResolvedAudioNode.class,
                ResolvedIntelLinkNode.class,
                ResolvedRedactedNode.class
        );

        for (Class<?> pureType : pureTypes) {
            for (Field field : pureType.getDeclaredFields()) {
                String fieldType = field.getType().getName();
                assertFalse(fieldType.startsWith("net.minecraft"));
                assertFalse(fieldType.startsWith("net.minecraftforge"));
            }
        }
    }

    @Test
    void resolveUseCaseCoordinatesDocumentContentAndPlayerState() {
        ArchiveContent content = new ArchiveContent(
                "content.test.v1",
                SimpleIntelService.TEST_DOCUMENT_ID,
                "v1",
                List.of(new TextContentNode("测试正文"))
        );
        SimpleInvestigationService investigationService = investigationService();
        ResolveArchiveContentUseCase useCase = new ResolveArchiveContentUseCase(
                new SimpleIntelService(),
                new SimpleArchiveContentService(List.of(content)),
                investigationService,
                new ArchiveContentResolver()
        );

        ResolveArchiveContentResult result = useCase.execute(
                UUID.randomUUID(),
                SimpleIntelService.TEST_DOCUMENT_ID
        );

        assertTrue(result.success());
        assertEquals(OperationStatus.SUCCESS, result.status());
        assertInstanceOf(
                ResolvedTextNode.class,
                result.content().orElseThrow().nodes().get(0)
        );
    }

    @Test
    void resolveUseCaseReportsMissingConfiguredContent() {
        ResolveArchiveContentUseCase useCase = new ResolveArchiveContentUseCase(
                new SimpleIntelService(),
                new SimpleArchiveContentService(List.of()),
                investigationService(),
                new ArchiveContentResolver()
        );

        ResolveArchiveContentResult result = useCase.execute(
                UUID.randomUUID(),
                SimpleIntelService.TEST_DOCUMENT_ID
        );

        assertFalse(result.success());
        assertEquals(OperationStatus.CONTENT_NOT_FOUND, result.status());
        assertTrue(result.content().isEmpty());
    }

    private static ArchiveContent contentWith(ContentNode... nodes) {
        return new ArchiveContent(
                "content.resolution.test.v1",
                "document.resolution.test",
                "v1",
                List.of(nodes)
        );
    }

    private static ResolvedArchiveContent resolveForNewPlayer(ArchiveContent content) {
        UUID playerId = UUID.randomUUID();
        SimpleInvestigationService investigationService = investigationService();
        return new ArchiveContentResolver().resolve(
                content,
                new PlayerContentContext(playerId, investigationService.getPlayerState(playerId))
        );
    }

    private static SimpleInvestigationService investigationService() {
        return new SimpleInvestigationService(Clock.fixed(
                Instant.parse("2026-08-16T05:00:00Z"),
                ZoneOffset.UTC
        ));
    }
}
