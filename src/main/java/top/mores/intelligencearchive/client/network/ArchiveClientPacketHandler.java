package top.mores.intelligencearchive.client.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import top.mores.intelligencearchive.client.ArchiveClient;
import top.mores.intelligencearchive.network.packet.ArchiveTestResponsePacket;
import top.mores.intelligencearchive.network.packet.ResponseArchiveDocumentPacket;
import top.mores.intelligencearchive.network.packet.ResponseResolvedArchiveContentPacket;

/**
 * 客户端 Packet 分发边界。
 * 调用方已经通过 enqueueWork 切换到客户端主线程，因此这里可以安全更新 UI 状态。
 */
@OnlyIn(Dist.CLIENT)
public final class ArchiveClientPacketHandler {
    private ArchiveClientPacketHandler() {
    }

    public static void handleTestResponse(ArchiveTestResponsePacket packet) {
        ArchiveClient.receiveTestResponse(packet);
    }

    public static void handleArchiveDocumentResponse(ResponseArchiveDocumentPacket packet) {
        ArchiveClient.receiveArchiveDocumentResponse(packet);
    }

    public static void handleResolvedArchiveContentResponse(ResponseResolvedArchiveContentPacket packet) {
        ArchiveClient.receiveResolvedArchiveContentResponse(packet);
    }
}
