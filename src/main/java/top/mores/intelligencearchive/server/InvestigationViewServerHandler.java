package top.mores.intelligencearchive.server;

import net.minecraft.server.level.ServerPlayer;
import top.mores.intelligencearchive.Intelligencearchive;
import top.mores.intelligencearchive.common.investigation.view.PlayerInvestigationView;
import top.mores.intelligencearchive.network.ArchiveNetwork;
import top.mores.intelligencearchive.network.packet.RequestInvestigationViewPacket;
import top.mores.intelligencearchive.network.packet.ResponseInvestigationViewPacket;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContext;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContexts;
import top.mores.intelligencearchive.server.mapper.InvestigationViewMapper;

import java.util.Optional;

/** 调查视图的服务端权威入口；玩家身份只取自当前网络连接。 */
public final class InvestigationViewServerHandler {
    private InvestigationViewServerHandler() {
    }

    public static void handleRequest(ServerPlayer player, RequestInvestigationViewPacket packet) {
        if (player == null) {
            Intelligencearchive.LOGGER.warn("[IntelligenceArchive] Ignored investigation view request without a sender");
            return;
        }

        try {
            Optional<ArchiveRuntimeContext> runtime = ArchiveRuntimeContexts.current();
            if (runtime.isEmpty()) {
                ArchiveNetwork.sendToPlayer(player, ResponseInvestigationViewPacket.failure(
                        "RUNTIME_NOT_READY",
                        "Archive runtime is not ready."
                ));
                return;
            }

            // playerId 只能来自 ServerPlayer，Request Packet 中不存在可伪造身份字段。
            Optional<PlayerInvestigationView> view = runtime.get()
                    .getInvestigationViewService()
                    .buildView(player.getUUID(), packet.caseId());
            if (view.isEmpty()) {
                ArchiveNetwork.sendToPlayer(player, ResponseInvestigationViewPacket.failure(
                        "CASE_NOT_FOUND",
                        "The requested investigation case does not exist."
                ));
                return;
            }

            ArchiveNetwork.sendToPlayer(
                    player,
                    ResponseInvestigationViewPacket.success(InvestigationViewMapper.toDto(view.get()))
            );
        } catch (IllegalArgumentException exception) {
            ArchiveNetwork.sendToPlayer(player, ResponseInvestigationViewPacket.failure(
                    "INVALID_REQUEST",
                    "The investigation view request is invalid."
            ));
        } catch (RuntimeException exception) {
            Intelligencearchive.LOGGER.error(
                    "[IntelligenceArchive] Failed to build investigation view for {}",
                    player.getUUID(),
                    exception
            );
            ArchiveNetwork.sendToPlayer(player, ResponseInvestigationViewPacket.failure(
                    "INTERNAL_ERROR",
                    "Server failed to build the investigation view."
            ));
        }
    }
}
