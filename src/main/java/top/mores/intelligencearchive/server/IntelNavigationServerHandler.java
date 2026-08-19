package top.mores.intelligencearchive.server;

import net.minecraft.server.level.ServerPlayer;
import top.mores.intelligencearchive.Intelligencearchive;
import top.mores.intelligencearchive.network.ArchiveNetwork;
import top.mores.intelligencearchive.network.packet.RequestIntelNavigationPacket;
import top.mores.intelligencearchive.network.packet.ResponseIntelNavigationPacket;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContext;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContexts;

import java.util.Optional;
import java.util.regex.Pattern;

/** IntelLink 请求的服务端鉴权入口，客户端无法自行决定跳转目标是否可见。 */
public final class IntelNavigationServerHandler {
    private static final Pattern INTEL_ID_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]*");

    private IntelNavigationServerHandler() {
    }

    public static void handleRequest(ServerPlayer player, RequestIntelNavigationPacket packet) {
        if (player == null) {
            Intelligencearchive.LOGGER.warn("[IntelligenceArchive] Ignored intel navigation request without a sender");
            return;
        }
        String targetId = packet.targetIntelId();
        if (!INTEL_ID_PATTERN.matcher(targetId).matches()) {
            ArchiveNetwork.sendToPlayer(player, ResponseIntelNavigationPacket.failure(
                    targetId,
                    "INVALID_INPUT",
                    "Invalid intel target ID."
            ));
            return;
        }

        try {
            Optional<ArchiveRuntimeContext> context = ArchiveRuntimeContexts.current();
            if (context.isEmpty()) {
                ArchiveNetwork.sendToPlayer(player, ResponseIntelNavigationPacket.failure(
                        targetId,
                        "RUNTIME_NOT_READY",
                        "Archive runtime is not ready."
                ));
                return;
            }
            ArchiveNetwork.sendToPlayer(
                    player,
                    ResponseIntelNavigationPacket.resolved(
                            context.get().getIntelNavigationService().resolve(player.getUUID(), targetId)
                    )
            );
        } catch (RuntimeException exception) {
            Intelligencearchive.LOGGER.error(
                    "[IntelligenceArchive] Failed to resolve intel navigation for {}",
                    player.getUUID(),
                    exception
            );
            ArchiveNetwork.sendToPlayer(player, ResponseIntelNavigationPacket.failure(
                    targetId,
                    "INTERNAL_ERROR",
                    "Server failed to resolve the intel target."
            ));
        }
    }
}
