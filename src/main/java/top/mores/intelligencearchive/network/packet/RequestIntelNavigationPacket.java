package top.mores.intelligencearchive.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import top.mores.intelligencearchive.server.IntelNavigationServerHandler;

import java.util.Objects;
import java.util.function.Supplier;

/** Client -> Server 的 IntelLink 导航请求；客户端只提交目标 ID。 */
public record RequestIntelNavigationPacket(String targetIntelId) {
    public static final int MAX_TARGET_ID_LENGTH = 128;

    public RequestIntelNavigationPacket {
        Objects.requireNonNull(targetIntelId, "targetIntelId 不能为 null");
        if (targetIntelId.isBlank() || targetIntelId.length() > MAX_TARGET_ID_LENGTH) {
            throw new IllegalArgumentException("targetIntelId 非法");
        }
    }

    public static void encode(RequestIntelNavigationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.targetIntelId, MAX_TARGET_ID_LENGTH);
    }

    public static RequestIntelNavigationPacket decode(FriendlyByteBuf buffer) {
        return new RequestIntelNavigationPacket(buffer.readUtf(MAX_TARGET_ID_LENGTH));
    }

    public static void handle(RequestIntelNavigationPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> IntelNavigationServerHandler.handleRequest(context.getSender(), packet));
        context.setPacketHandled(true);
    }
}
