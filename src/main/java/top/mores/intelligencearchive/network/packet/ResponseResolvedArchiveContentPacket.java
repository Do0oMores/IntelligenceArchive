package top.mores.intelligencearchive.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import top.mores.intelligencearchive.client.network.ArchiveClientPacketHandler;
import top.mores.intelligencearchive.common.dto.ResolvedArchiveContentDTO;
import top.mores.intelligencearchive.network.codec.ResolvedArchiveContentDtoCodec;

import java.util.Objects;
import java.util.function.Supplier;

/** Server -> Client：只传输已经由服务端解析的玩家可见 DTO。 */
public record ResponseResolvedArchiveContentPacket(
        boolean success,
        String resultCode,
        String documentId,
        ResolvedArchiveContentDTO content,
        String message
) {
    public static final int MAX_RESULT_CODE_LENGTH = 64;
    public static final int MAX_MESSAGE_LENGTH = 256;

    public ResponseResolvedArchiveContentPacket {
        resultCode = requireText(resultCode, "resultCode", MAX_RESULT_CODE_LENGTH, false);
        documentId = requireText(
                documentId,
                "documentId",
                ResolvedArchiveContentDTO.MAX_ID_LENGTH,
                true
        );
        message = requireText(message, "message", MAX_MESSAGE_LENGTH, true);
        if (success) {
            content = Objects.requireNonNull(content, "成功响应必须包含 content");
            if (!documentId.equals(content.documentId())) {
                throw new IllegalArgumentException("响应 documentId 与 DTO 不一致");
            }
        } else if (content != null) {
            throw new IllegalArgumentException("失败响应不能包含 content");
        }
    }

    public static ResponseResolvedArchiveContentPacket success(ResolvedArchiveContentDTO content) {
        ResolvedArchiveContentDTO valid = Objects.requireNonNull(content, "content 不能为 null");
        return new ResponseResolvedArchiveContentPacket(
                true,
                "SUCCESS",
                valid.documentId(),
                valid,
                ""
        );
    }

    public static ResponseResolvedArchiveContentPacket failure(
            String resultCode,
            String documentId,
            String message
    ) {
        return new ResponseResolvedArchiveContentPacket(false, resultCode, documentId, null, message);
    }

    public static void encode(ResponseResolvedArchiveContentPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.success);
        buffer.writeUtf(packet.resultCode, MAX_RESULT_CODE_LENGTH);
        buffer.writeUtf(packet.documentId, ResolvedArchiveContentDTO.MAX_ID_LENGTH);
        buffer.writeUtf(packet.message, MAX_MESSAGE_LENGTH);
        if (packet.success) {
            ResolvedArchiveContentDtoCodec.encode(packet.content, buffer);
        }
    }

    public static ResponseResolvedArchiveContentPacket decode(FriendlyByteBuf buffer) {
        boolean success = buffer.readBoolean();
        String resultCode = buffer.readUtf(MAX_RESULT_CODE_LENGTH);
        String documentId = buffer.readUtf(ResolvedArchiveContentDTO.MAX_ID_LENGTH);
        String message = buffer.readUtf(MAX_MESSAGE_LENGTH);
        if (success) {
            return new ResponseResolvedArchiveContentPacket(
                    true,
                    resultCode,
                    documentId,
                    ResolvedArchiveContentDtoCodec.decode(buffer),
                    message
            );
        }
        return failure(resultCode, documentId, message);
    }

    /** 客户端主线程只更新临时状态，不在 Network Thread 操作 Screen。 */
    public static void handle(
            ResponseResolvedArchiveContentPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ArchiveClientPacketHandler.handleResolvedArchiveContentResponse(packet)
        ));
        context.setPacketHandled(true);
    }

    private static String requireText(
            String value,
            String fieldName,
            int maxLength,
            boolean allowEmpty
    ) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (!allowEmpty && value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " 长度不能超过 " + maxLength);
        }
        return value;
    }
}
