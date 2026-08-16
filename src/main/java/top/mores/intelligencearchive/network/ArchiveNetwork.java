package top.mores.intelligencearchive.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import top.mores.intelligencearchive.Intelligencearchive;
import top.mores.intelligencearchive.network.packet.RequestArchiveDocumentPacket;
import top.mores.intelligencearchive.network.packet.RequestResolvedArchiveContentPacket;
import top.mores.intelligencearchive.network.packet.ResponseArchiveDocumentPacket;
import top.mores.intelligencearchive.network.packet.ResponseResolvedArchiveContentPacket;
import top.mores.intelligencearchive.network.packet.RequestArchiveIndexPacket;
import top.mores.intelligencearchive.network.packet.ResponseArchiveIndexPacket;
import top.mores.intelligencearchive.network.packet.RequestIntelNavigationPacket;
import top.mores.intelligencearchive.network.packet.ResponseIntelNavigationPacket;
import top.mores.intelligencearchive.network.packet.RequestInvestigationViewPacket;
import top.mores.intelligencearchive.network.packet.ResponseInvestigationViewPacket;

/**
 * IntelligenceArchive 网络层的唯一注册入口。
 *
 * <p>协议版本和消息 ID 集中在这里，后续新增档案、节点或关系包时可以继续使用
 * 独立 Packet，而不需要引入依赖字符串类型分派的“万能包”。</p>
 */
public final class ArchiveNetwork {
    // Phase 5-C-2B 扩展调查展示 DTO，必须拒绝仍按旧字段布局解码的客户端。
    private static final String PROTOCOL_VERSION = "5";

    // 显式固定消息 ID，避免重排注册代码时意外破坏线上协议。
    // 0、1 曾用于 Phase 1 通道测试，删除后永久保留，避免重排正式消息 ID。
    private static final int ARCHIVE_DOCUMENT_REQUEST_ID = 2;
    private static final int ARCHIVE_DOCUMENT_RESPONSE_ID = 3;
    private static final int RESOLVED_CONTENT_REQUEST_ID = 4;
    private static final int RESOLVED_CONTENT_RESPONSE_ID = 5;
    private static final int ARCHIVE_INDEX_REQUEST_ID = 6;
    private static final int ARCHIVE_INDEX_RESPONSE_ID = 7;
    private static final int INTEL_NAVIGATION_REQUEST_ID = 8;
    private static final int INTEL_NAVIGATION_RESPONSE_ID = 9;
    private static final int INVESTIGATION_VIEW_REQUEST_ID = 10;
    private static final int INVESTIGATION_VIEW_RESPONSE_ID = 11;

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
                        RequestArchiveIndexPacket.class,
                        ARCHIVE_INDEX_REQUEST_ID,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(RequestArchiveIndexPacket::encode)
                .decoder(RequestArchiveIndexPacket::decode)
                .consumerNetworkThread(RequestArchiveIndexPacket::handle)
                .add();

        CHANNEL.messageBuilder(
                        ResponseArchiveIndexPacket.class,
                        ARCHIVE_INDEX_RESPONSE_ID,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(ResponseArchiveIndexPacket::encode)
                .decoder(ResponseArchiveIndexPacket::decode)
                .consumerNetworkThread(ResponseArchiveIndexPacket::handle)
                .add();

        CHANNEL.messageBuilder(
                        RequestIntelNavigationPacket.class,
                        INTEL_NAVIGATION_REQUEST_ID,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(RequestIntelNavigationPacket::encode)
                .decoder(RequestIntelNavigationPacket::decode)
                .consumerNetworkThread(RequestIntelNavigationPacket::handle)
                .add();

        CHANNEL.messageBuilder(
                        ResponseIntelNavigationPacket.class,
                        INTEL_NAVIGATION_RESPONSE_ID,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(ResponseIntelNavigationPacket::encode)
                .decoder(ResponseIntelNavigationPacket::decode)
                .consumerNetworkThread(ResponseIntelNavigationPacket::handle)
                .add();

        CHANNEL.messageBuilder(
                        RequestInvestigationViewPacket.class,
                        INVESTIGATION_VIEW_REQUEST_ID,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(RequestInvestigationViewPacket::encode)
                .decoder(RequestInvestigationViewPacket::decode)
                .consumerNetworkThread(RequestInvestigationViewPacket::handle)
                .add();

        CHANNEL.messageBuilder(
                        ResponseInvestigationViewPacket.class,
                        INVESTIGATION_VIEW_RESPONSE_ID,
                        NetworkDirection.PLAY_TO_CLIENT
                )
                .encoder(ResponseInvestigationViewPacket::encode)
                .decoder(ResponseInvestigationViewPacket::decode)
                .consumerNetworkThread(ResponseInvestigationViewPacket::handle)
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

    public static void sendToServer(RequestArchiveIndexPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, ResponseArchiveIndexPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(RequestIntelNavigationPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, ResponseIntelNavigationPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(RequestInvestigationViewPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, ResponseInvestigationViewPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
