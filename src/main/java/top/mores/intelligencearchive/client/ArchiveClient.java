package top.mores.intelligencearchive.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.mores.intelligencearchive.Intelligencearchive;
import top.mores.intelligencearchive.client.key.ArchiveKeyMappings;
import top.mores.intelligencearchive.client.investigation.ClientInvestigationState;
import top.mores.intelligencearchive.client.investigation.InvestigationScreen;
import top.mores.intelligencearchive.client.screen.ArchiveScreen;
import top.mores.intelligencearchive.client.state.ArchiveClientArchiveState;
import top.mores.intelligencearchive.client.state.ArchiveIndexState;
import top.mores.intelligencearchive.client.state.IntelNavigationState;
import top.mores.intelligencearchive.client.view.ArchiveViewModel;
import top.mores.intelligencearchive.client.view.ArchiveViewModelMapper;
import top.mores.intelligencearchive.client.view.ArchiveSummaryViewModelMapper;
import top.mores.intelligencearchive.network.ArchiveNetwork;
import top.mores.intelligencearchive.network.packet.RequestArchiveDocumentPacket;
import top.mores.intelligencearchive.network.packet.RequestResolvedArchiveContentPacket;
import top.mores.intelligencearchive.network.packet.ResponseArchiveDocumentPacket;
import top.mores.intelligencearchive.network.packet.ResponseResolvedArchiveContentPacket;
import top.mores.intelligencearchive.network.packet.RequestArchiveIndexPacket;
import top.mores.intelligencearchive.network.packet.ResponseArchiveIndexPacket;
import top.mores.intelligencearchive.network.packet.RequestIntelNavigationPacket;
import top.mores.intelligencearchive.network.packet.ResponseIntelNavigationPacket;
import top.mores.intelligencearchive.network.packet.RequestInvestigationViewPacket;
import top.mores.intelligencearchive.network.packet.ResponseInvestigationViewPacket;

/**
 * 客户端协调层：处理按键、Screen 打开动作和临时响应状态。
 *
 * <p>整个类只在 Dist.CLIENT 的 Forge 事件总线上加载，公共初始化路径不会解析
 * Minecraft 或 Screen 等客户端专属类。</p>
 */
@Mod.EventBusSubscriber(modid = Intelligencearchive.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArchiveClient {
    private static final ArchiveClientArchiveState ARCHIVE_STATE = new ArchiveClientArchiveState();
    private static final ArchiveIndexState INDEX_STATE = new ArchiveIndexState();
    private static final IntelNavigationState NAVIGATION_STATE = new IntelNavigationState();
    private static final ClientInvestigationState INVESTIGATION_STATE = new ClientInvestigationState();

    private ArchiveClient() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ARCHIVE_STATE.tick(System.nanoTime());
        INDEX_STATE.tick(System.nanoTime());
        NAVIGATION_STATE.tick(System.nanoTime());
        INVESTIGATION_STATE.tick(System.nanoTime());

        while (ArchiveKeyMappings.OPEN_ARCHIVE.consumeClick()) {
            // 不覆盖聊天、背包或其他 MOD 的界面；玩家进入世界后才能打开档案。
            if (minecraft.player != null && minecraft.screen == null) {
                minecraft.setScreen(new ArchiveScreen());
            }
        }

        while (ArchiveKeyMappings.OPEN_INVESTIGATION.consumeClick()) {
            // 调查终端与档案阅读终端是两个入口，均不覆盖玩家当前打开的其他 GUI。
            if (minecraft.player != null && minecraft.screen == null) {
                minecraft.setScreen(new InvestigationScreen());
            }
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        // 临时 UI 状态不得跨服务器连接泄漏。
        ARCHIVE_STATE.reset();
        INDEX_STATE.reset();
        NAVIGATION_STATE.reset();
        INVESTIGATION_STATE.reset();
    }

    /** 请求指定档案元数据；调用方只能提交 ID，不能提交权限或解锁结论。 */
    public static void requestArchiveDocument(String documentId) {
        if (documentId == null || documentId.isBlank()
                || !ARCHIVE_STATE.beginRequest(documentId, System.nanoTime())) {
            return;
        }

        try {
            ArchiveNetwork.sendToServer(new RequestArchiveDocumentPacket(documentId));
        } catch (RuntimeException exception) {
            ARCHIVE_STATE.failToSend(documentId);
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
    public static void requestResolvedContent(String documentId) {
        if (documentId == null || documentId.isBlank()
                || !ARCHIVE_STATE.beginResolvedRequest(documentId, System.nanoTime())) {
            return;
        }

        try {
            ArchiveNetwork.sendToServer(new RequestResolvedArchiveContentPacket(documentId));
        } catch (RuntimeException exception) {
            ARCHIVE_STATE.failResolvedSend(documentId);
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

    /** 打开终端或刷新时请求服务端已过滤的摘要索引。 */
    public static void requestArchiveIndex() {
        if (!INDEX_STATE.beginRequest(System.nanoTime())) {
            return;
        }
        try {
            ArchiveNetwork.sendToServer(new RequestArchiveIndexPacket());
        } catch (RuntimeException exception) {
            INDEX_STATE.fail("Unable to send the archive index request to the server.");
            Intelligencearchive.LOGGER.warn("[IntelligenceArchive] Failed to send archive index request", exception);
        }
    }

    public static void receiveArchiveIndexResponse(ResponseArchiveIndexPacket packet) {
        if (!packet.success()) {
            INDEX_STATE.fail(packet.message());
            return;
        }
        try {
            INDEX_STATE.accept(packet.archives().stream()
                    .map(ArchiveSummaryViewModelMapper::fromDto)
                    .toList());
        } catch (RuntimeException exception) {
            INDEX_STATE.fail("The server archive index could not be displayed.");
            Intelligencearchive.LOGGER.warn("[IntelligenceArchive] Failed to map archive index", exception);
        }
    }

    public static ArchiveIndexState.View getArchiveIndexViewState() {
        return INDEX_STATE.view();
    }

    public static IntelNavigationState.View getIntelNavigationViewState() {
        return NAVIGATION_STATE.view();
    }

    /** IntelLink 点击只发出目标 ID；是否可导航以及返回哪些信息仍由服务端决定。 */
    public static void handleIntelLinkClick(String intelId) {
        if (intelId == null || intelId.isBlank()
                || !NAVIGATION_STATE.beginRequest(intelId, System.nanoTime())) {
            return;
        }
        ARCHIVE_STATE.recordIntelLinkClick(intelId);
        try {
            ArchiveNetwork.sendToServer(new RequestIntelNavigationPacket(intelId));
        } catch (RuntimeException exception) {
            NAVIGATION_STATE.fail(intelId, "Unable to send the intel navigation request.");
            Intelligencearchive.LOGGER.warn("[IntelligenceArchive] Failed to send intel navigation request", exception);
        }
    }

    public static void receiveIntelNavigationResponse(ResponseIntelNavigationPacket packet) {
        NAVIGATION_STATE.accept(packet);
        // 服务端明确返回 ARCHIVE 文档 ID 后才进入现有详情请求；详情端点仍会再次鉴权。
        if (packet.success()
                && packet.targetType() == top.mores.intelligencearchive.common.model.IntelNavigationTargetType.ARCHIVE
                && !packet.documentId().isBlank()) {
            requestResolvedContent(packet.documentId());
        }
    }

    /** 请求当前玩家在指定 Case 下的服务端安全调查投影，不提交 playerId 或任何状态。 */
    public static void requestInvestigationView(String caseId) {
        if (caseId == null || caseId.isBlank()
                || !INVESTIGATION_STATE.beginRequest(caseId, System.nanoTime())) {
            return;
        }
        try {
            ArchiveNetwork.sendToServer(new RequestInvestigationViewPacket(caseId));
        } catch (RuntimeException exception) {
            INVESTIGATION_STATE.fail("Unable to send the investigation view request.");
            Intelligencearchive.LOGGER.warn(
                    "[IntelligenceArchive] Failed to send investigation view request",
                    exception
            );
        }
    }

    public static void receiveInvestigationViewResponse(ResponseInvestigationViewPacket packet) {
        if (packet.success()) {
            INVESTIGATION_STATE.accept(packet.viewDTO());
        } else {
            INVESTIGATION_STATE.fail(packet.message());
        }
    }

    public static ClientInvestigationState.View getInvestigationViewState() {
        return INVESTIGATION_STATE.view();
    }
}
