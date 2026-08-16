package top.mores.intelligencearchive.server;

import net.minecraft.server.level.ServerPlayer;
import top.mores.intelligencearchive.Intelligencearchive;
import top.mores.intelligencearchive.application.result.ResolveArchiveContentResult;
import top.mores.intelligencearchive.common.dto.ResolvedArchiveContentDTO;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;
import top.mores.intelligencearchive.network.ArchiveNetwork;
import top.mores.intelligencearchive.network.packet.RequestResolvedArchiveContentPacket;
import top.mores.intelligencearchive.network.packet.ResponseResolvedArchiveContentPacket;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContext;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContexts;
import top.mores.intelligencearchive.server.mapper.ResolvedArchiveContentMapper;

import java.util.Optional;
import java.util.regex.Pattern;

/** Resolved 内容请求的服务端权威入口。 */
public final class ResolvedArchiveContentServerHandler {
    private static final Pattern DOCUMENT_ID_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]*");

    private ResolvedArchiveContentServerHandler() {
    }

    public static void handleRequest(
            ServerPlayer player,
            RequestResolvedArchiveContentPacket packet
    ) {
        if (player == null) {
            Intelligencearchive.LOGGER.warn("[IntelligenceArchive] Ignored resolved content request without sender");
            return;
        }
        Intelligencearchive.LOGGER.info(
                "[IntelligenceArchive] Received resolved archive content request from {}",
                player.getUUID()
        );
        String documentId = packet.documentId();
        if (!DOCUMENT_ID_PATTERN.matcher(documentId).matches()) {
            ArchiveNetwork.sendToPlayer(player, ResponseResolvedArchiveContentPacket.failure(
                    "INVALID_INPUT",
                    documentId,
                    "Invalid document ID."
            ));
            return;
        }

        try {
            Optional<ArchiveRuntimeContext> runtimeContext = ArchiveRuntimeContexts.current();
            if (runtimeContext.isEmpty()) {
                ArchiveNetwork.sendToPlayer(player, ResponseResolvedArchiveContentPacket.failure(
                        "RUNTIME_NOT_READY",
                        documentId,
                        "Archive runtime is not ready."
                ));
                return;
            }
            ArchiveRuntimeContext context = runtimeContext.get();
            ResolveArchiveContentResult result = context
                    .getResolveArchiveContentUseCase()
                    .execute(player.getUUID(), documentId);
            if (!result.success()) {
                ArchiveNetwork.sendToPlayer(player, ResponseResolvedArchiveContentPacket.failure(
                        result.status().name(),
                        documentId,
                        result.message()
                ));
                return;
            }

            ResolvedArchiveContentDTO dto = ResolvedArchiveContentMapper.toDto(
                    result.content().orElseThrow()
            );
            // 成功阅读详情后由服务端推进 READ；客户端不能自行提交阅读状态。
            if (context.getInvestigationService().getPlayerState(player.getUUID()).statusOf(documentId)
                    == IntelDiscoveryStatus.DISCOVERED) {
                context.getReadArchiveUseCase().execute(player.getUUID(), documentId);
            }
            ArchiveNetwork.sendToPlayer(player, ResponseResolvedArchiveContentPacket.success(dto));
            Intelligencearchive.LOGGER.info(
                    "[IntelligenceArchive] Sent resolved archive content {} to {}",
                    documentId,
                    player.getUUID()
            );
        } catch (RuntimeException exception) {
            Intelligencearchive.LOGGER.error(
                    "[IntelligenceArchive] Failed to resolve archive content for {}",
                    player.getUUID(),
                    exception
            );
            ArchiveNetwork.sendToPlayer(player, ResponseResolvedArchiveContentPacket.failure(
                    "INTERNAL_ERROR",
                    documentId,
                    "Server failed to resolve archive content."
            ));
        }
    }
}
