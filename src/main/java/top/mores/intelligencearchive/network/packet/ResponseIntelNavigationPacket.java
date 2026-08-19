package top.mores.intelligencearchive.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import top.mores.intelligencearchive.client.network.ArchiveClientPacketHandler;
import top.mores.intelligencearchive.common.model.IntelNavigationResult;
import top.mores.intelligencearchive.common.model.IntelNavigationTargetType;

import java.util.Objects;
import java.util.function.Supplier;

/** Server -> Client 的 IntelLink 安全解析结果。 */
public record ResponseIntelNavigationPacket(
        boolean success,
        String resultCode,
        IntelNavigationTargetType targetType,
        String targetId,
        String title,
        String description,
        String documentId,
        String message
) {
    public static final int MAX_RESULT_CODE_LENGTH = 64;
    public static final int MAX_TITLE_LENGTH = 256;
    public static final int MAX_DESCRIPTION_LENGTH = 512;
    public static final int MAX_MESSAGE_LENGTH = 256;

    public ResponseIntelNavigationPacket {
        resultCode = requireText(resultCode, "resultCode", MAX_RESULT_CODE_LENGTH, false);
        targetType = Objects.requireNonNull(targetType, "targetType 不能为 null");
        targetId = requireText(targetId, "targetId", RequestIntelNavigationPacket.MAX_TARGET_ID_LENGTH, false);
        title = requireText(title, "title", MAX_TITLE_LENGTH, true);
        description = requireText(description, "description", MAX_DESCRIPTION_LENGTH, true);
        documentId = requireText(documentId, "documentId", RequestArchiveDocumentPacket.MAX_DOCUMENT_ID_LENGTH, true);
        message = requireText(message, "message", MAX_MESSAGE_LENGTH, true);
        if (!success && targetType != IntelNavigationTargetType.UNKNOWN) {
            throw new IllegalArgumentException("失败响应不能包含已解析目标");
        }
        if (targetType == IntelNavigationTargetType.ARCHIVE && documentId.isBlank()) {
            throw new IllegalArgumentException("ARCHIVE 导航响应必须包含 documentId");
        }
    }

    public static ResponseIntelNavigationPacket resolved(IntelNavigationResult result) {
        Objects.requireNonNull(result, "result 不能为 null");
        String code = result.targetType() == IntelNavigationTargetType.UNKNOWN ? "UNKNOWN_TARGET" : "SUCCESS";
        String message = result.targetType() == IntelNavigationTargetType.UNKNOWN
                ? "Unknown target."
                : "Intel navigation resolved.";
        return new ResponseIntelNavigationPacket(
                true,
                code,
                result.targetType(),
                result.targetId(),
                result.title(),
                result.description(),
                result.documentId(),
                message
        );
    }

    public static ResponseIntelNavigationPacket failure(String targetId, String resultCode, String message) {
        return new ResponseIntelNavigationPacket(
                false,
                resultCode,
                IntelNavigationTargetType.UNKNOWN,
                targetId,
                "",
                "",
                "",
                message
        );
    }

    public static void encode(ResponseIntelNavigationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.success);
        buffer.writeUtf(packet.resultCode, MAX_RESULT_CODE_LENGTH);
        buffer.writeEnum(packet.targetType);
        buffer.writeUtf(packet.targetId, RequestIntelNavigationPacket.MAX_TARGET_ID_LENGTH);
        buffer.writeUtf(packet.title, MAX_TITLE_LENGTH);
        buffer.writeUtf(packet.description, MAX_DESCRIPTION_LENGTH);
        buffer.writeUtf(packet.documentId, RequestArchiveDocumentPacket.MAX_DOCUMENT_ID_LENGTH);
        buffer.writeUtf(packet.message, MAX_MESSAGE_LENGTH);
    }

    public static ResponseIntelNavigationPacket decode(FriendlyByteBuf buffer) {
        return new ResponseIntelNavigationPacket(
                buffer.readBoolean(),
                buffer.readUtf(MAX_RESULT_CODE_LENGTH),
                buffer.readEnum(IntelNavigationTargetType.class),
                buffer.readUtf(RequestIntelNavigationPacket.MAX_TARGET_ID_LENGTH),
                buffer.readUtf(MAX_TITLE_LENGTH),
                buffer.readUtf(MAX_DESCRIPTION_LENGTH),
                buffer.readUtf(RequestArchiveDocumentPacket.MAX_DOCUMENT_ID_LENGTH),
                buffer.readUtf(MAX_MESSAGE_LENGTH)
        );
    }

    public static void handle(ResponseIntelNavigationPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ArchiveClientPacketHandler.handleIntelNavigationResponse(packet)
        ));
        context.setPacketHandled(true);
    }

    private static String requireText(String value, String fieldName, int maxLength, boolean allowEmpty) {
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
