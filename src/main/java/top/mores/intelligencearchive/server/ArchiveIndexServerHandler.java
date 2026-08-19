package top.mores.intelligencearchive.server;

import net.minecraft.server.level.ServerPlayer;
import top.mores.intelligencearchive.Intelligencearchive;
import top.mores.intelligencearchive.common.dto.ArchiveSummaryDTO;
import top.mores.intelligencearchive.network.ArchiveNetwork;
import top.mores.intelligencearchive.network.packet.ResponseArchiveIndexPacket;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContext;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContexts;
import top.mores.intelligencearchive.server.mapper.ArchiveSummaryMapper;

import java.util.List;
import java.util.Optional;

/** 档案索引请求的服务端权威入口。 */
public final class ArchiveIndexServerHandler {
    private ArchiveIndexServerHandler() {
    }

    public static void handleRequest(ServerPlayer player) {
        if (player == null) {
            Intelligencearchive.LOGGER.warn("[IntelligenceArchive] Ignored archive index request without a sender");
            return;
        }

        try {
            Optional<ArchiveRuntimeContext> context = ArchiveRuntimeContexts.current();
            if (context.isEmpty()) {
                ArchiveNetwork.sendToPlayer(player, ResponseArchiveIndexPacket.failure(
                        "RUNTIME_NOT_READY",
                        "Archive runtime is not ready."
                ));
                return;
            }

            List<ArchiveSummaryDTO> archives = context.get()
                    .getArchiveIndexService()
                    .findVisibleArchives(player.getUUID(), ResponseArchiveIndexPacket.MAX_ARCHIVE_COUNT)
                    .stream()
                    .map(ArchiveSummaryMapper::toDto)
                    .toList();
            ArchiveNetwork.sendToPlayer(player, ResponseArchiveIndexPacket.success(archives));
            Intelligencearchive.LOGGER.info(
                    "[IntelligenceArchive] Sent {} visible archive summaries to {}",
                    archives.size(),
                    player.getUUID()
            );
        } catch (RuntimeException exception) {
            Intelligencearchive.LOGGER.error(
                    "[IntelligenceArchive] Failed to build archive index for {}",
                    player.getUUID(),
                    exception
            );
            ArchiveNetwork.sendToPlayer(player, ResponseArchiveIndexPacket.failure(
                    "INTERNAL_ERROR",
                    "Server failed to build the archive index."
            ));
        }
    }
}
