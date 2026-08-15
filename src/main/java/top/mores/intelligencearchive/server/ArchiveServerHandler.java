package top.mores.intelligencearchive.server;

import net.minecraft.server.level.ServerPlayer;
import top.mores.intelligencearchive.Intelligencearchive;
import top.mores.intelligencearchive.network.ArchiveNetwork;
import top.mores.intelligencearchive.network.packet.ArchiveTestRequestPacket;
import top.mores.intelligencearchive.network.packet.ArchiveTestResponsePacket;

/**
 * 服务端业务入口。
 *
 * <p>这里是 server-authoritative 边界：即使客户端请求格式合法，服务端仍须自行验证，
 * 未来的权限、解锁与剧情状态也只能在这一侧裁决。</p>
 */
public final class ArchiveServerHandler {
    private ArchiveServerHandler() {
    }

    public static void handleTestRequest(ServerPlayer player, ArchiveTestRequestPacket request) {
        if (player == null) {
            Intelligencearchive.LOGGER.warn("[IntelligenceArchive] Ignored archive test request without a sender");
            return;
        }

        Intelligencearchive.LOGGER.info(
                "[IntelligenceArchive] Received archive test request from {}",
                player.getUUID()
        );

        if (request.requestId() <= 0) {
            ArchiveNetwork.sendToPlayer(player, new ArchiveTestResponsePacket(
                    request.requestId(),
                    "Archive test request was rejected: invalid request id.",
                    false
            ));
            return;
        }

        String playerName = player.getGameProfile().getName();
        String message = "Hello " + playerName + ",\n"
                + "Intelligence Archive server channel is working.";

        ArchiveNetwork.sendToPlayer(player, new ArchiveTestResponsePacket(
                request.requestId(),
                message,
                true
        ));
    }
}
