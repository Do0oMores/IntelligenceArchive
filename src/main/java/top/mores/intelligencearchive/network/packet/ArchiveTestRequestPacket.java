package top.mores.intelligencearchive.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import top.mores.intelligencearchive.server.ArchiveServerHandler;

import java.util.function.Supplier;

/**
 * Client -> Server 的连通性测试请求。
 *
 * <p>客户端只提交用于匹配响应的 requestId，绝不提交 unlock、permission 等
 * 应由服务端裁决的业务结论。</p>
 */
public record ArchiveTestRequestPacket(int requestId) {
    public static void encode(ArchiveTestRequestPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.requestId);
    }

    public static ArchiveTestRequestPacket decode(FriendlyByteBuf buffer) {
        return new ArchiveTestRequestPacket(buffer.readInt());
    }

    /**
     * Forge 的消息消费者运行在网络线程；必须 enqueueWork 后才能安全读取玩家并执行服务端逻辑。
     * Packet 本身只做解码后的分发，验证和响应构造由 server 层负责。
     */
    public static void handle(ArchiveTestRequestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ArchiveServerHandler.handleTestRequest(context.getSender(), packet));
        context.setPacketHandled(true);
    }
}
