package top.mores.intelligencearchive.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.mores.intelligencearchive.Intelligencearchive;
import top.mores.intelligencearchive.client.key.ArchiveKeyMappings;
import top.mores.intelligencearchive.client.screen.ArchiveScreen;
import top.mores.intelligencearchive.client.state.ArchiveClientArchiveState;
import top.mores.intelligencearchive.client.state.ArchiveClientState;
import top.mores.intelligencearchive.client.view.ArchiveViewModel;
import top.mores.intelligencearchive.client.view.ArchiveViewModelMapper;
import top.mores.intelligencearchive.network.ArchiveNetwork;
import top.mores.intelligencearchive.network.packet.ArchiveTestRequestPacket;
import top.mores.intelligencearchive.network.packet.ArchiveTestResponsePacket;
import top.mores.intelligencearchive.network.packet.RequestArchiveDocumentPacket;
import top.mores.intelligencearchive.network.packet.RequestResolvedArchiveContentPacket;
import top.mores.intelligencearchive.network.packet.ResponseArchiveDocumentPacket;
import top.mores.intelligencearchive.network.packet.ResponseResolvedArchiveContentPacket;

/**
 * 客户端协调层：处理按键、Screen 打开动作和临时响应状态。
 *
 * <p>整个类只在 Dist.CLIENT 的 Forge 事件总线上加载，公共初始化路径不会解析
 * Minecraft 或 Screen 等客户端专属类。</p>
 */
@Mod.EventBusSubscriber(modid = Intelligencearchive.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArchiveClient {
    private static final String TEST_DOCUMENT_ID = "document.case.test_001";
    private static final ArchiveClientState STATE = new ArchiveClientState();
    private static final ArchiveClientArchiveState ARCHIVE_STATE = new ArchiveClientArchiveState();

    private ArchiveClient() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        STATE.tick(System.nanoTime());
        ARCHIVE_STATE.tick(System.nanoTime());

        while (ArchiveKeyMappings.OPEN_ARCHIVE.consumeClick()) {
            // 不覆盖聊天、背包或其他 MOD 的界面；玩家进入世界后才能打开档案。
            if (minecraft.player != null && minecraft.screen == null) {
                minecraft.setScreen(new ArchiveScreen());
            }
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        // 临时 UI 状态不得跨服务器连接泄漏。
        STATE.reset();
        ARCHIVE_STATE.reset();
    }

    public static void requestServerTest() {
        int requestId = STATE.beginRequest(System.nanoTime());
        if (requestId < 0) {
            return;
        }

        try {
            ArchiveNetwork.sendToServer(new ArchiveTestRequestPacket(requestId));
        } catch (RuntimeException exception) {
            STATE.failRequest(requestId, "Unable to send the request to the server.");
            Intelligencearchive.LOGGER.warn("[IntelligenceArchive] Failed to send archive test request", exception);
        }
    }

    public static void receiveTestResponse(ArchiveTestResponsePacket packet) {
        STATE.acceptResponse(packet);
    }

    public static ArchiveClientState.View getViewState() {
        return STATE.view();
    }

    public static void requestTestDocument() {
        if (!ARCHIVE_STATE.beginRequest(TEST_DOCUMENT_ID, System.nanoTime())) {
            return;
        }

        try {
            ArchiveNetwork.sendToServer(new RequestArchiveDocumentPacket(TEST_DOCUMENT_ID));
        } catch (RuntimeException exception) {
            ARCHIVE_STATE.failToSend(TEST_DOCUMENT_ID);
            Intelligencearchive.LOGGER.warn("[IntelligenceArchive] Failed to send archive document request", exception);
        }
    }

    public static void receiveArchiveDocumentResponse(ResponseArchiveDocumentPacket packet) {
        if (packet.success()) {
            ARCHIVE_STATE.acceptDocument(packet.documentId(), packet.document());
        } else {
            ARCHIVE_STATE.acceptError(packet.documentId(), packet.errorMessage());
        }
    }

    public static ArchiveClientArchiveState.View getArchiveViewState() {
        return ARCHIVE_STATE.view();
    }

    /**
     * 请求服务端已完成权限与条件裁决的展示内容。
     * 客户端只提交文档 ID，绝不提交 unlock、permission 等业务结论。
     */
    public static void requestResolvedTestContent() {
        if (!ARCHIVE_STATE.beginResolvedRequest(TEST_DOCUMENT_ID, System.nanoTime())) {
            return;
        }

        try {
            ArchiveNetwork.sendToServer(new RequestResolvedArchiveContentPacket(TEST_DOCUMENT_ID));
        } catch (RuntimeException exception) {
            ARCHIVE_STATE.failResolvedSend(TEST_DOCUMENT_ID);
            Intelligencearchive.LOGGER.warn(
                    "[IntelligenceArchive] Failed to send resolved archive content request",
                    exception
            );
        }
    }

    /** Packet 已经在客户端主线程入队；此处只做 DTO 到只读 ViewModel 的转换。 */
    public static void receiveResolvedArchiveContentResponse(ResponseResolvedArchiveContentPacket packet) {
        if (!packet.success()) {
            ARCHIVE_STATE.acceptResolvedError(packet.documentId(), packet.message());
            return;
        }

        try {
            ArchiveViewModel viewModel = ArchiveViewModelMapper.fromDto(packet.content());
            ARCHIVE_STATE.acceptResolvedContent(packet.documentId(), viewModel);
        } catch (RuntimeException exception) {
            ARCHIVE_STATE.acceptResolvedError(packet.documentId(), "The server response could not be displayed.");
            Intelligencearchive.LOGGER.warn(
                    "[IntelligenceArchive] Failed to map resolved archive content response",
                    exception
            );
        }
    }

    public static ArchiveClientArchiveState.ResolvedView getResolvedArchiveViewState() {
        return ARCHIVE_STATE.resolvedView();
    }

    /** Phase 3-C-2 只预留点击入口，不在客户端据此改变情报拥有状态。 */
    public static void handleIntelLinkClick(String intelId) {
        ARCHIVE_STATE.recordIntelLinkClick(intelId);
        Intelligencearchive.LOGGER.info("[IntelligenceArchive] Intel link clicked: {}", intelId);
    }
}
