package top.mores.intelligencearchive.server;

import net.minecraft.server.level.ServerPlayer;
import top.mores.intelligencearchive.Intelligencearchive;
import top.mores.intelligencearchive.common.model.ArchiveDocument;
import top.mores.intelligencearchive.common.service.IntelService;
import top.mores.intelligencearchive.common.service.InvestigationService;
import top.mores.intelligencearchive.network.ArchiveNetwork;
import top.mores.intelligencearchive.network.packet.RequestArchiveDocumentPacket;
import top.mores.intelligencearchive.network.packet.ResponseArchiveDocumentPacket;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContext;
import top.mores.intelligencearchive.server.context.ArchiveRuntimeContexts;
import top.mores.intelligencearchive.server.mapper.ArchiveDocumentMapper;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 档案查询的服务端权威处理器。
 *
 * <p>处理器验证客户端输入后只能通过 Runtime Context 中共享的 {@link IntelService} 查询，
 * 不自行创建 Service，也不接触 Repository 的 Map。</p>
 */
public final class ArchiveDocumentServerHandler {
    private static final Pattern DOCUMENT_ID_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]*");

    private ArchiveDocumentServerHandler() {
    }

    public static void handleRequest(ServerPlayer player, RequestArchiveDocumentPacket request) {
        if (player == null) {
            Intelligencearchive.LOGGER.warn("[IntelligenceArchive] Ignored archive document request without a sender");
            return;
        }

        String documentId = request.documentId();
        try {
            Optional<ArchiveRuntimeContext> runtimeContext = ArchiveRuntimeContexts.current();
            if (runtimeContext.isEmpty()) {
                Intelligencearchive.LOGGER.warn(
                        "[IntelligenceArchive] Ignored archive request before runtime context initialization"
                );
                ArchiveNetwork.sendToPlayer(player, ResponseArchiveDocumentPacket.failure(
                        safeResponseId(documentId),
                        "Archive runtime is not ready."
                ));
                return;
            }

            ResponseArchiveDocumentPacket response = createResponse(
                    documentId,
                    player.getUUID(),
                    runtimeContext.get().getIntelService(),
                    runtimeContext.get().getInvestigationService()
            );
            if (isValidDocumentId(documentId)) {
                Intelligencearchive.LOGGER.info(
                        "[IntelligenceArchive] Received archive document request {} from {}",
                        documentId,
                        player.getUUID()
                );
            }
            ArchiveNetwork.sendToPlayer(player, response);
        } catch (RuntimeException exception) {
            Intelligencearchive.LOGGER.error(
                    "[IntelligenceArchive] Failed to query archive document for {}",
                    player.getUUID(),
                    exception
            );
            ArchiveNetwork.sendToPlayer(player, ResponseArchiveDocumentPacket.failure(
                    safeResponseId(documentId),
                    "Server failed to query the document."
            ));
        }
    }

    /** 包可见方法用于验证“请求 -> Service -> Mapper -> Response”业务链。 */
    static ResponseArchiveDocumentPacket createResponse(
            String documentId,
            java.util.UUID playerId,
            IntelService intelService,
            InvestigationService investigationService
    ) {
        if (!isValidDocumentId(documentId)) {
            return ResponseArchiveDocumentPacket.failure(safeResponseId(documentId), "Invalid document id.");
        }

        // 不区分“未发现”和“不存在”，避免元数据接口成为枚举世界档案的旁路。
        if (!investigationService.hasDiscovered(playerId, documentId)) {
            return ResponseArchiveDocumentPacket.failure(documentId, "Document not found.");
        }

        Optional<ArchiveDocument> document = intelService.findDocumentById(documentId);
        return document
                .map(ArchiveDocumentMapper::toDto)
                .map(ResponseArchiveDocumentPacket::success)
                .orElseGet(() -> ResponseArchiveDocumentPacket.failure(documentId, "Document not found."));
    }

    private static boolean isValidDocumentId(String documentId) {
        return documentId != null
                && !documentId.isBlank()
                && documentId.length() <= RequestArchiveDocumentPacket.MAX_DOCUMENT_ID_LENGTH
                && DOCUMENT_ID_PATTERN.matcher(documentId).matches();
    }

    private static String safeResponseId(String documentId) {
        if (documentId == null) {
            return "";
        }
        return documentId.substring(0, Math.min(
                documentId.length(),
                RequestArchiveDocumentPacket.MAX_DOCUMENT_ID_LENGTH
        ));
    }
}
