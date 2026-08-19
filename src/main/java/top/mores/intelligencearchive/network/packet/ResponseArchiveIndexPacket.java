package top.mores.intelligencearchive.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import top.mores.intelligencearchive.client.network.ArchiveClientPacketHandler;
import top.mores.intelligencearchive.common.dto.ArchiveSummaryDTO;
import top.mores.intelligencearchive.common.model.ArchiveSummaryStatus;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Server -> Client 的轻量档案索引响应，绝不携带档案正文。 */
public record ResponseArchiveIndexPacket(
        boolean success,
        String resultCode,
        List<ArchiveSummaryDTO> archives,
        String message
) {
    public static final int MAX_ARCHIVE_COUNT = 50;
    public static final int MAX_RESULT_CODE_LENGTH = 64;
    public static final int MAX_MESSAGE_LENGTH = 256;

    public ResponseArchiveIndexPacket {
        resultCode = requireText(resultCode, "resultCode", MAX_RESULT_CODE_LENGTH, false);
        message = requireText(message, "message", MAX_MESSAGE_LENGTH, true);
        archives = List.copyOf(Objects.requireNonNull(archives, "archives 不能为 null"));
        if (archives.size() > MAX_ARCHIVE_COUNT) {
            throw new IllegalArgumentException("档案索引数量不能超过 " + MAX_ARCHIVE_COUNT);
        }
        if (!success && !archives.isEmpty()) {
            throw new IllegalArgumentException("失败响应不能包含档案摘要");
        }
    }

    public static ResponseArchiveIndexPacket success(List<ArchiveSummaryDTO> archives) {
        return new ResponseArchiveIndexPacket(true, "SUCCESS", archives, "Archive index loaded.");
    }

    public static ResponseArchiveIndexPacket failure(String resultCode, String message) {
        return new ResponseArchiveIndexPacket(false, resultCode, List.of(), message);
    }

    public static void encode(ResponseArchiveIndexPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.success);
        buffer.writeUtf(packet.resultCode, MAX_RESULT_CODE_LENGTH);
        buffer.writeUtf(packet.message, MAX_MESSAGE_LENGTH);
        buffer.writeVarInt(packet.archives.size());
        for (ArchiveSummaryDTO archive : packet.archives) {
            encodeSummary(archive, buffer);
        }
    }

    public static ResponseArchiveIndexPacket decode(FriendlyByteBuf buffer) {
        boolean success = buffer.readBoolean();
        String resultCode = buffer.readUtf(MAX_RESULT_CODE_LENGTH);
        String message = buffer.readUtf(MAX_MESSAGE_LENGTH);
        int count = buffer.readVarInt();
        if (count < 0 || count > MAX_ARCHIVE_COUNT) {
            throw new IllegalArgumentException("非法档案索引数量: " + count);
        }
        java.util.ArrayList<ArchiveSummaryDTO> archives = new java.util.ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            archives.add(decodeSummary(buffer));
        }
        return new ResponseArchiveIndexPacket(success, resultCode, archives, message);
    }

    private static void encodeSummary(ArchiveSummaryDTO summary, FriendlyByteBuf buffer) {
        buffer.writeUtf(summary.documentId(), ArchiveSummaryDTO.MAX_DOCUMENT_ID_LENGTH);
        buffer.writeUtf(summary.title(), ArchiveSummaryDTO.MAX_TITLE_LENGTH);
        buffer.writeUtf(summary.type(), ArchiveSummaryDTO.MAX_TYPE_LENGTH);
        buffer.writeUtf(summary.securityLevel(), ArchiveSummaryDTO.MAX_SECURITY_LEVEL_LENGTH);
        buffer.writeUtf(summary.summary(), ArchiveSummaryDTO.MAX_SUMMARY_LENGTH);
        buffer.writeEnum(summary.status());
        buffer.writeUtf(summary.version(), ArchiveSummaryDTO.MAX_VERSION_LENGTH);
    }

    private static ArchiveSummaryDTO decodeSummary(FriendlyByteBuf buffer) {
        return new ArchiveSummaryDTO(
                buffer.readUtf(ArchiveSummaryDTO.MAX_DOCUMENT_ID_LENGTH),
                buffer.readUtf(ArchiveSummaryDTO.MAX_TITLE_LENGTH),
                buffer.readUtf(ArchiveSummaryDTO.MAX_TYPE_LENGTH),
                buffer.readUtf(ArchiveSummaryDTO.MAX_SECURITY_LEVEL_LENGTH),
                buffer.readUtf(ArchiveSummaryDTO.MAX_SUMMARY_LENGTH),
                buffer.readEnum(ArchiveSummaryStatus.class),
                buffer.readUtf(ArchiveSummaryDTO.MAX_VERSION_LENGTH)
        );
    }

    public static void handle(ResponseArchiveIndexPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        // 客户端状态与 Screen 只能在客户端主线程更新。
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ArchiveClientPacketHandler.handleArchiveIndexResponse(packet)
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
