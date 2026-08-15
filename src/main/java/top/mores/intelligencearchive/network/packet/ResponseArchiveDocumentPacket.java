package top.mores.intelligencearchive.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import top.mores.intelligencearchive.client.network.ArchiveClientPacketHandler;
import top.mores.intelligencearchive.common.dto.ArchiveDocumentDTO;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Server -> Client 的档案查询结果。
 *
 * <p>Packet 传输专用 DTO，而不是 {@code ArchiveDocument}，避免把服务端领域模型、
 * Repository 结构或未来内部字段固化进网络协议。</p>
 */
public record ResponseArchiveDocumentPacket(
        boolean success,
        String documentId,
        ArchiveDocumentDTO document,
        String errorMessage
) {
    public static final int MAX_ERROR_MESSAGE_LENGTH = 256;

    public ResponseArchiveDocumentPacket {
        documentId = requireText(documentId, "documentId", RequestArchiveDocumentPacket.MAX_DOCUMENT_ID_LENGTH);
        errorMessage = requireText(errorMessage, "errorMessage", MAX_ERROR_MESSAGE_LENGTH);

        if (success) {
            document = Objects.requireNonNull(document, "成功响应必须包含 document");
            if (!documentId.equals(document.id())) {
                throw new IllegalArgumentException("响应 documentId 必须与 DTO id 一致");
            }
            if (!errorMessage.isEmpty()) {
                throw new IllegalArgumentException("成功响应不能包含 errorMessage");
            }
        } else {
            if (document != null) {
                throw new IllegalArgumentException("失败响应不能包含 document");
            }
            if (errorMessage.isBlank()) {
                throw new IllegalArgumentException("失败响应必须包含 errorMessage");
            }
        }
    }

    public static ResponseArchiveDocumentPacket success(ArchiveDocumentDTO document) {
        ArchiveDocumentDTO validDocument = Objects.requireNonNull(document, "document 不能为 null");
        return new ResponseArchiveDocumentPacket(true, validDocument.id(), validDocument, "");
    }

    public static ResponseArchiveDocumentPacket failure(String documentId, String errorMessage) {
        return new ResponseArchiveDocumentPacket(false, documentId, null, errorMessage);
    }

    public static void encode(ResponseArchiveDocumentPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.success);
        buffer.writeUtf(packet.documentId, RequestArchiveDocumentPacket.MAX_DOCUMENT_ID_LENGTH);

        if (packet.success) {
            encodeDocument(packet.document, buffer);
        } else {
            buffer.writeUtf(packet.errorMessage, MAX_ERROR_MESSAGE_LENGTH);
        }
    }

    public static ResponseArchiveDocumentPacket decode(FriendlyByteBuf buffer) {
        boolean success = buffer.readBoolean();
        String documentId = buffer.readUtf(RequestArchiveDocumentPacket.MAX_DOCUMENT_ID_LENGTH);

        if (success) {
            return new ResponseArchiveDocumentPacket(true, documentId, decodeDocument(buffer), "");
        }
        return failure(documentId, buffer.readUtf(MAX_ERROR_MESSAGE_LENGTH));
    }

    private static void encodeDocument(ArchiveDocumentDTO document, FriendlyByteBuf buffer) {
        buffer.writeUtf(document.id(), ArchiveDocumentDTO.MAX_ID_LENGTH);
        buffer.writeUtf(document.title(), ArchiveDocumentDTO.MAX_TITLE_LENGTH);
        buffer.writeUtf(document.type(), ArchiveDocumentDTO.MAX_TYPE_LENGTH);
        buffer.writeUtf(document.summary(), ArchiveDocumentDTO.MAX_SUMMARY_LENGTH);
        buffer.writeUtf(document.contentReference(), ArchiveDocumentDTO.MAX_CONTENT_REFERENCE_LENGTH);
        buffer.writeLong(document.createdTimeEpochMillis());
        buffer.writeUtf(document.author(), ArchiveDocumentDTO.MAX_AUTHOR_LENGTH);
        buffer.writeUtf(document.securityLevel(), ArchiveDocumentDTO.MAX_SECURITY_LEVEL_LENGTH);
    }

    private static ArchiveDocumentDTO decodeDocument(FriendlyByteBuf buffer) {
        return new ArchiveDocumentDTO(
                buffer.readUtf(ArchiveDocumentDTO.MAX_ID_LENGTH),
                buffer.readUtf(ArchiveDocumentDTO.MAX_TITLE_LENGTH),
                buffer.readUtf(ArchiveDocumentDTO.MAX_TYPE_LENGTH),
                buffer.readUtf(ArchiveDocumentDTO.MAX_SUMMARY_LENGTH),
                buffer.readUtf(ArchiveDocumentDTO.MAX_CONTENT_REFERENCE_LENGTH),
                buffer.readLong(),
                buffer.readUtf(ArchiveDocumentDTO.MAX_AUTHOR_LENGTH),
                buffer.readUtf(ArchiveDocumentDTO.MAX_SECURITY_LEVEL_LENGTH)
        );
    }

    /** 响应先切换到客户端主线程，再更新轻量 ClientState，不直接操作 Screen。 */
    public static void handle(
            ResponseArchiveDocumentPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ArchiveClientPacketHandler.handleArchiveDocumentResponse(packet)
        ));
        context.setPacketHandled(true);
    }

    private static String requireText(String value, String fieldName, int maxLength) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " 长度不能超过 " + maxLength);
        }
        return value;
    }
}
