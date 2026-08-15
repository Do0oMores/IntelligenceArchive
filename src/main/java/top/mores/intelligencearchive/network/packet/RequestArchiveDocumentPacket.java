package top.mores.intelligencearchive.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import top.mores.intelligencearchive.server.ArchiveDocumentServerHandler;

import java.util.function.Supplier;

/**
 * Client -> Server 的档案查询请求。
 *
 * <p>客户端只能提交档案 ID，不能提交 unlock、permission 或其他业务裁决结果。</p>
 */
public record RequestArchiveDocumentPacket(String documentId) {
    public static final int MAX_DOCUMENT_ID_LENGTH = 128;

    public static void encode(RequestArchiveDocumentPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.documentId, MAX_DOCUMENT_ID_LENGTH);
    }

    public static RequestArchiveDocumentPacket decode(FriendlyByteBuf buffer) {
        return new RequestArchiveDocumentPacket(buffer.readUtf(MAX_DOCUMENT_ID_LENGTH));
    }

    /** 网络线程只负责分发，查询与玩家访问都在 enqueueWork 切换后的服务端主线程执行。 */
    public static void handle(
            RequestArchiveDocumentPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ArchiveDocumentServerHandler.handleRequest(context.getSender(), packet));
        context.setPacketHandled(true);
    }
}
