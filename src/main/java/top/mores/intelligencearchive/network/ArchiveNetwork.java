package top.mores.intelligencearchive.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import top.mores.intelligencearchive.Intelligencearchive;
import top.mores.intelligencearchive.network.packet.ArchiveTestRequestPacket;
import top.mores.intelligencearchive.network.packet.ArchiveTestResponsePacket;
import top.mores.intelligencearchive.network.packet.RequestArchiveDocumentPacket;
import top.mores.intelligencearchive.network.packet.RequestResolvedArchiveContentPacket;
import top.mores.intelligencearchive.network.packet.ResponseArchiveDocumentPacket;
import top.mores.intelligencearchive.network.packet.ResponseResolvedArchiveContentPacket;

/**
 * IntelligenceArchive 网络层的唯一注册入口。
 *
 * <p>协议版本和消息 ID 集中在这里，后续新增档案、节点或关系包时可以继续使用
 * 独立 Packet，而不需要引入依赖字符串类型分派的“万能包”。</p>
 */
public final class ArchiveNetwork {
    // 新增 resolved-content 消息后协议与旧 Phase 客户端不再对称，必须拒绝误兼容连接。
    private static final String PROTOCOL_VERSION = "2";

    // 显式固定消息 ID，避免重排注册代码时意外破坏线上协议。
    private static final int TEST_REQUEST_ID = 0;
    private static final int TEST_RESPONSE_ID = 1;
    private static final int ARCHIVE_DOCUMENT_REQUEST_ID = 2;
    private static final int ARCHIVE_DOCUMENT_RESPONSE_ID = 3;
    private static final int RESOLVED_CONTENT_REQUEST_ID = 4;
    private static final int RESOLVED_CONTENT_RESPONSE_ID = 5;

    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(Intelligencearchive.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static boolean initialized;

    private ArchiveNetwork() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        CHANNEL.messageBuilder(ArchiveTestRequestPacket.class, TEST_REQUEST_ID, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ArchiveTestRequestPacket::encode)
                .decoder(ArchiveTestRequestPacket::decode)
                .consumerNetworkThread(ArchiveTestRequestPacket::handle)
                .add();

        CHANNEL.messageBuilder(ArchiveTestResponsePacket.class, TEST_RESPONSE_ID, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ArchiveTestResponsePacket::encode)
                .decoder(ArchiveTestResponsePacket::decode)
                .consumerNetworkThread(ArchiveTestResponsePacket::handle)
                .add();

        CHANNEL.messageBuilder(
                        RequestArchiveDocumentPacket.class,
                        ARCHIVE_DOCUMENT_REQUEST_ID,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(RequestArchiveDocumentPacket::encode)
                .decoder(RequestArchiveDocumentPacket::decode)
                .consumerNetworkThread(RequestArchiveDocumentPacket::handle)
                .add();

        CHANNEL.messageBuilder(
                        ResponseArchiveDocumentPacket.class,
                        ARCHIVE_DOCUMENT_RESPONSE_ID,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(ResponseArchiveDocumentPacket::encode)
                .decoder(ResponseArchiveDocumentPacket::decode)
                .consumerNetworkThread(ResponseArchiveDocumentPacket::handle)
                .add();

        CHANNEL.messageBuilder(
                        RequestResolvedArchiveContentPacket.class,
                        RESOLVED_CONTENT_REQUEST_ID,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(RequestResolvedArchiveContentPacket::encode)
                .decoder(RequestResolvedArchiveContentPacket::decode)
                .consumerNetworkThread(RequestResolvedArchiveContentPacket::handle)
                .add();

        CHANNEL.messageBuilder(
                        ResponseResolvedArchiveContentPacket.class,
                        RESOLVED_CONTENT_RESPONSE_ID,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(ResponseResolvedArchiveContentPacket::encode)
                .decoder(ResponseResolvedArchiveContentPacket::decode)
                .consumerNetworkThread(ResponseResolvedArchiveContentPacket::handle)
                .add();

        initialized = true;
        Intelligencearchive.LOGGER.info("[IntelligenceArchive] Network channel initialized");
    }

    /** 仅供客户端入口调用，发送的 requestId 只是关联请求，不代表任何业务结果。 */
    public static void sendToServer(ArchiveTestRequestPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    /** 服务端只把响应发回发起请求的玩家。 */
    public static void sendToPlayer(ServerPlayer player, ArchiveTestResponsePacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    /** 客户端请求指定 ID 的档案；请求不携带任何授权或解锁结论。 */
    public static void sendToServer(RequestArchiveDocumentPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    /** 将档案查询结果只发送给当前请求玩家。 */
    public static void sendToPlayer(ServerPlayer player, ResponseArchiveDocumentPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    /** 客户端只请求文档 ID，所有可见内容由服务端解析。 */
    public static void sendToServer(RequestResolvedArchiveContentPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    /** 只向请求玩家发送已经解析的安全内容 DTO。 */
    public static void sendToPlayer(ServerPlayer player, ResponseResolvedArchiveContentPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
