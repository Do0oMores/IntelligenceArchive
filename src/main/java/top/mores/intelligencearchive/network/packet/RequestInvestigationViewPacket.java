package top.mores.intelligencearchive.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import top.mores.intelligencearchive.server.InvestigationViewServerHandler;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Client -> Server 的调查视图请求。
 *
 * <p>包体只有 caseId；playerId 必须从连接对应的 ServerPlayer 获取，客户端不能提交状态或关系。</p>
 */
public record RequestInvestigationViewPacket(String caseId) {
    public static final int MAX_CASE_ID_LENGTH = 128;
    private static final Pattern CASE_ID_PATTERN = Pattern.compile("[a-z0-9][a-z0-9._-]*");

    public RequestInvestigationViewPacket {
        Objects.requireNonNull(caseId, "caseId 不能为 null");
        if (caseId.isBlank()
                || caseId.length() > MAX_CASE_ID_LENGTH
                || !CASE_ID_PATTERN.matcher(caseId).matches()) {
            throw new IllegalArgumentException("caseId 非法");
        }
    }

    public static void encode(RequestInvestigationViewPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.caseId, MAX_CASE_ID_LENGTH);
    }

    public static RequestInvestigationViewPacket decode(FriendlyByteBuf buffer) {
        return new RequestInvestigationViewPacket(buffer.readUtf(MAX_CASE_ID_LENGTH));
    }

    public static void handle(
            RequestInvestigationViewPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        // View 读取涉及玩家状态，必须从网络线程切回服务端主线程。
        context.enqueueWork(() -> InvestigationViewServerHandler.handleRequest(context.getSender(), packet));
        context.setPacketHandled(true);
    }
}
