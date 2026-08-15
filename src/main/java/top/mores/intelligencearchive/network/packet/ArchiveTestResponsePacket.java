package top.mores.intelligencearchive.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import top.mores.intelligencearchive.client.network.ArchiveClientPacketHandler;

import java.util.function.Supplier;

/** Server -> Client 的连通性测试响应。 */
public record ArchiveTestResponsePacket(int requestId, String message, boolean serverAccepted) {
    private static final int MAX_MESSAGE_LENGTH = 512;

    public static void encode(ArchiveTestResponsePacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.requestId);
        buffer.writeUtf(packet.message, MAX_MESSAGE_LENGTH);
        buffer.writeBoolean(packet.serverAccepted);
    }

    public static ArchiveTestResponsePacket decode(FriendlyByteBuf buffer) {
        return new ArchiveTestResponsePacket(
                buffer.readInt(),
                buffer.readUtf(MAX_MESSAGE_LENGTH),
                buffer.readBoolean()
        );
    }

    /**
     * 网络线程不能直接操作 UI。enqueueWork 会把状态更新排入客户端主线程；
     * DistExecutor 则确保 Dedicated Server 永远不会加载客户端处理类。
     */
    public static void handle(ArchiveTestResponsePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ArchiveClientPacketHandler.handleTestResponse(packet)
        ));
        context.setPacketHandled(true);
    }
}
