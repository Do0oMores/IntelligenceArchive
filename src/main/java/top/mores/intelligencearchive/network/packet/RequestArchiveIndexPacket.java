package top.mores.intelligencearchive.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import top.mores.intelligencearchive.server.ArchiveIndexServerHandler;

import java.util.function.Supplier;

/**
 * Client -> Server 的档案索引请求。
 *
 * <p>请求没有字段。玩家身份必须从连接对应的 ServerPlayer 取得，客户端不能提交
 * documentIds、权限或过滤结果。</p>
 */
public record RequestArchiveIndexPacket() {
    public static void encode(RequestArchiveIndexPacket packet, FriendlyByteBuf buffer) {
        // 空包体是有意设计：索引范围完全由服务端玩家上下文决定。
    }

    public static RequestArchiveIndexPacket decode(FriendlyByteBuf buffer) {
        return new RequestArchiveIndexPacket();
    }

    public static void handle(RequestArchiveIndexPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        // Packet consumer 运行在网络线程，所有玩家状态读取都必须切回服务端主线程。
        context.enqueueWork(() -> ArchiveIndexServerHandler.handleRequest(context.getSender()));
        context.setPacketHandled(true);
    }
}
