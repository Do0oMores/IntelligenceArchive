package top.mores.intelligencearchive.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import top.mores.intelligencearchive.common.dto.ResolvedArchiveContentDTO;
import top.mores.intelligencearchive.server.ResolvedArchiveContentServerHandler;

import java.util.Objects;
import java.util.function.Supplier;

/** Client -> Server：只请求 documentId，不携带权限、条件或可见性结论。 */
public record RequestResolvedArchiveContentPacket(String documentId) {
    public RequestResolvedArchiveContentPacket {
        Objects.requireNonNull(documentId, "documentId 不能为 null");
        if (documentId.isBlank() || documentId.length() > ResolvedArchiveContentDTO.MAX_ID_LENGTH) {
            throw new IllegalArgumentException("documentId 无效");
        }
    }

    public static void encode(RequestResolvedArchiveContentPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.documentId, ResolvedArchiveContentDTO.MAX_ID_LENGTH);
    }

    public static RequestResolvedArchiveContentPacket decode(FriendlyByteBuf buffer) {
        return new RequestResolvedArchiveContentPacket(
                buffer.readUtf(ResolvedArchiveContentDTO.MAX_ID_LENGTH)
        );
    }

    /** Network Thread 只解码，服务端业务在 enqueueWork 后执行。 */
    public static void handle(
            RequestResolvedArchiveContentPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ResolvedArchiveContentServerHandler.handleRequest(
                context.getSender(),
                packet
        ));
        context.setPacketHandled(true);
    }
}
