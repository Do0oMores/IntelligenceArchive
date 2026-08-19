package top.mores.intelligencearchive.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import top.mores.intelligencearchive.client.network.ArchiveClientPacketHandler;
import top.mores.intelligencearchive.common.dto.investigation.InvestigationClueViewDTO;
import top.mores.intelligencearchive.common.dto.investigation.InvestigationEvidenceViewDTO;
import top.mores.intelligencearchive.common.dto.investigation.InvestigationHypothesisViewDTO;
import top.mores.intelligencearchive.common.dto.investigation.InvestigationNodeViewDTO;
import top.mores.intelligencearchive.common.dto.investigation.InvestigationRelationViewDTO;
import top.mores.intelligencearchive.common.dto.investigation.PlayerInvestigationViewDTO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Server -> Client 的安全调查视图响应，只携带经过 Projection 和 Mapper 过滤的 DTO。 */
public record ResponseInvestigationViewPacket(
        boolean success,
        String resultCode,
        PlayerInvestigationViewDTO viewDTO,
        String message
) {
    public static final int MAX_RESULT_CODE_LENGTH = 64;
    public static final int MAX_MESSAGE_LENGTH = 256;

    public ResponseInvestigationViewPacket {
        resultCode = requireText(resultCode, "resultCode", MAX_RESULT_CODE_LENGTH, false);
        message = requireText(message, "message", MAX_MESSAGE_LENGTH, true);
        if (success && viewDTO == null) {
            throw new IllegalArgumentException("成功响应必须包含 viewDTO");
        }
        if (!success && viewDTO != null) {
            throw new IllegalArgumentException("失败响应不能包含 viewDTO");
        }
    }

    public static ResponseInvestigationViewPacket success(PlayerInvestigationViewDTO viewDTO) {
        return new ResponseInvestigationViewPacket(
                true,
                "SUCCESS",
                Objects.requireNonNull(viewDTO, "viewDTO 不能为 null"),
                "Investigation view loaded."
        );
    }

    public static ResponseInvestigationViewPacket failure(String resultCode, String message) {
        return new ResponseInvestigationViewPacket(false, resultCode, null, message);
    }

    public static void encode(ResponseInvestigationViewPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.success);
        buffer.writeUtf(packet.resultCode, MAX_RESULT_CODE_LENGTH);
        buffer.writeUtf(packet.message, MAX_MESSAGE_LENGTH);
        buffer.writeBoolean(packet.viewDTO != null);
        if (packet.viewDTO != null) {
            encodeView(packet.viewDTO, buffer);
        }
    }

    public static ResponseInvestigationViewPacket decode(FriendlyByteBuf buffer) {
        boolean success = buffer.readBoolean();
        String resultCode = buffer.readUtf(MAX_RESULT_CODE_LENGTH);
        String message = buffer.readUtf(MAX_MESSAGE_LENGTH);
        PlayerInvestigationViewDTO view = buffer.readBoolean() ? decodeView(buffer) : null;
        return new ResponseInvestigationViewPacket(success, resultCode, view, message);
    }

    private static void encodeView(PlayerInvestigationViewDTO view, FriendlyByteBuf buffer) {
        buffer.writeUtf(view.caseId(), PlayerInvestigationViewDTO.MAX_CASE_ID_LENGTH);
        buffer.writeUtf(view.caseTitle(), PlayerInvestigationViewDTO.MAX_CASE_TITLE_LENGTH);
        buffer.writeUtf(view.caseStatus(), PlayerInvestigationViewDTO.MAX_CASE_STATUS_LENGTH);
        writeInstant(buffer, view.timestamp());

        buffer.writeVarInt(view.nodes().size());
        for (InvestigationNodeViewDTO node : view.nodes()) {
            buffer.writeUtf(node.intelId(), InvestigationNodeViewDTO.MAX_ID_LENGTH);
            buffer.writeUtf(node.displayName(), InvestigationNodeViewDTO.MAX_DISPLAY_NAME_LENGTH);
            buffer.writeUtf(node.type(), InvestigationNodeViewDTO.MAX_ENUM_NAME_LENGTH);
            buffer.writeUtf(node.status(), InvestigationNodeViewDTO.MAX_ENUM_NAME_LENGTH);
            buffer.writeUtf(node.importance(), InvestigationNodeViewDTO.MAX_ENUM_NAME_LENGTH);
        }

        buffer.writeVarInt(view.relations().size());
        for (InvestigationRelationViewDTO relation : view.relations()) {
            buffer.writeUtf(relation.sourceId(), InvestigationRelationViewDTO.MAX_ID_LENGTH);
            buffer.writeUtf(relation.targetId(), InvestigationRelationViewDTO.MAX_ID_LENGTH);
            buffer.writeUtf(relation.relationType(), InvestigationRelationViewDTO.MAX_ENUM_NAME_LENGTH);
            buffer.writeUtf(relation.confidence(), InvestigationRelationViewDTO.MAX_ENUM_NAME_LENGTH);
            writeInstant(buffer, relation.createdTime());
        }

        buffer.writeVarInt(view.evidence().size());
        for (InvestigationEvidenceViewDTO evidence : view.evidence()) {
            buffer.writeUtf(evidence.id(), InvestigationEvidenceViewDTO.MAX_ID_LENGTH);
            buffer.writeUtf(evidence.title(), InvestigationEvidenceViewDTO.MAX_TITLE_LENGTH);
            buffer.writeUtf(evidence.sourceType(), InvestigationEvidenceViewDTO.MAX_ENUM_NAME_LENGTH);
            buffer.writeUtf(evidence.importance(), InvestigationEvidenceViewDTO.MAX_ENUM_NAME_LENGTH);
        }

        buffer.writeVarInt(view.clues().size());
        for (InvestigationClueViewDTO clue : view.clues()) {
            buffer.writeUtf(clue.id(), InvestigationClueViewDTO.MAX_ID_LENGTH);
            buffer.writeUtf(clue.title(), InvestigationClueViewDTO.MAX_TITLE_LENGTH);
            buffer.writeUtf(clue.importance(), InvestigationClueViewDTO.MAX_ENUM_NAME_LENGTH);
            buffer.writeUtf(clue.reliability(), InvestigationClueViewDTO.MAX_ENUM_NAME_LENGTH);
        }

        buffer.writeVarInt(view.hypotheses().size());
        for (InvestigationHypothesisViewDTO hypothesis : view.hypotheses()) {
            buffer.writeUtf(hypothesis.id(), InvestigationHypothesisViewDTO.MAX_ID_LENGTH);
            buffer.writeUtf(hypothesis.title(), InvestigationHypothesisViewDTO.MAX_TITLE_LENGTH);
            buffer.writeUtf(hypothesis.status(), InvestigationHypothesisViewDTO.MAX_STATUS_LENGTH);
            buffer.writeUtf(hypothesis.confidence(), InvestigationHypothesisViewDTO.MAX_STATUS_LENGTH);
        }
    }

    private static PlayerInvestigationViewDTO decodeView(FriendlyByteBuf buffer) {
        String caseId = buffer.readUtf(PlayerInvestigationViewDTO.MAX_CASE_ID_LENGTH);
        String caseTitle = buffer.readUtf(PlayerInvestigationViewDTO.MAX_CASE_TITLE_LENGTH);
        String caseStatus = buffer.readUtf(PlayerInvestigationViewDTO.MAX_CASE_STATUS_LENGTH);
        Instant timestamp = readInstant(buffer);

        int nodeCount = readCount(buffer, "node", PlayerInvestigationViewDTO.MAX_NODE_COUNT);
        List<InvestigationNodeViewDTO> nodes = new ArrayList<>(nodeCount);
        for (int index = 0; index < nodeCount; index++) {
            nodes.add(new InvestigationNodeViewDTO(
                    buffer.readUtf(InvestigationNodeViewDTO.MAX_ID_LENGTH),
                    buffer.readUtf(InvestigationNodeViewDTO.MAX_DISPLAY_NAME_LENGTH),
                    buffer.readUtf(InvestigationNodeViewDTO.MAX_ENUM_NAME_LENGTH),
                    buffer.readUtf(InvestigationNodeViewDTO.MAX_ENUM_NAME_LENGTH),
                    buffer.readUtf(InvestigationNodeViewDTO.MAX_ENUM_NAME_LENGTH)
            ));
        }

        int relationCount = readCount(buffer, "relation", PlayerInvestigationViewDTO.MAX_RELATION_COUNT);
        List<InvestigationRelationViewDTO> relations = new ArrayList<>(relationCount);
        for (int index = 0; index < relationCount; index++) {
            relations.add(new InvestigationRelationViewDTO(
                    buffer.readUtf(InvestigationRelationViewDTO.MAX_ID_LENGTH),
                    buffer.readUtf(InvestigationRelationViewDTO.MAX_ID_LENGTH),
                    buffer.readUtf(InvestigationRelationViewDTO.MAX_ENUM_NAME_LENGTH),
                    buffer.readUtf(InvestigationRelationViewDTO.MAX_ENUM_NAME_LENGTH),
                    readInstant(buffer)
            ));
        }

        int evidenceCount = readCount(buffer, "evidence", PlayerInvestigationViewDTO.MAX_EVIDENCE_COUNT);
        List<InvestigationEvidenceViewDTO> evidence = new ArrayList<>(evidenceCount);
        for (int index = 0; index < evidenceCount; index++) {
            evidence.add(new InvestigationEvidenceViewDTO(
                    buffer.readUtf(InvestigationEvidenceViewDTO.MAX_ID_LENGTH),
                    buffer.readUtf(InvestigationEvidenceViewDTO.MAX_TITLE_LENGTH),
                    buffer.readUtf(InvestigationEvidenceViewDTO.MAX_ENUM_NAME_LENGTH),
                    buffer.readUtf(InvestigationEvidenceViewDTO.MAX_ENUM_NAME_LENGTH)
            ));
        }

        int clueCount = readCount(buffer, "clue", PlayerInvestigationViewDTO.MAX_CLUE_COUNT);
        List<InvestigationClueViewDTO> clues = new ArrayList<>(clueCount);
        for (int index = 0; index < clueCount; index++) {
            clues.add(new InvestigationClueViewDTO(
                    buffer.readUtf(InvestigationClueViewDTO.MAX_ID_LENGTH),
                    buffer.readUtf(InvestigationClueViewDTO.MAX_TITLE_LENGTH),
                    buffer.readUtf(InvestigationClueViewDTO.MAX_ENUM_NAME_LENGTH),
                    buffer.readUtf(InvestigationClueViewDTO.MAX_ENUM_NAME_LENGTH)
            ));
        }

        int hypothesisCount = readCount(
                buffer,
                "hypothesis",
                PlayerInvestigationViewDTO.MAX_HYPOTHESIS_COUNT
        );
        List<InvestigationHypothesisViewDTO> hypotheses = new ArrayList<>(hypothesisCount);
        for (int index = 0; index < hypothesisCount; index++) {
            hypotheses.add(new InvestigationHypothesisViewDTO(
                    buffer.readUtf(InvestigationHypothesisViewDTO.MAX_ID_LENGTH),
                    buffer.readUtf(InvestigationHypothesisViewDTO.MAX_TITLE_LENGTH),
                    buffer.readUtf(InvestigationHypothesisViewDTO.MAX_STATUS_LENGTH),
                    buffer.readUtf(InvestigationHypothesisViewDTO.MAX_STATUS_LENGTH)
            ));
        }

        return new PlayerInvestigationViewDTO(
                caseId,
                caseTitle,
                caseStatus,
                nodes,
                relations,
                evidence,
                clues,
                hypotheses,
                timestamp
        );
    }

    private static int readCount(FriendlyByteBuf buffer, String name, int maximum) {
        int count = buffer.readVarInt();
        if (count < 0 || count > maximum) {
            throw new IllegalArgumentException("非法 " + name + " 数量: " + count);
        }
        return count;
    }

    private static void writeInstant(FriendlyByteBuf buffer, Instant value) {
        buffer.writeLong(value.getEpochSecond());
        buffer.writeInt(value.getNano());
    }

    private static Instant readInstant(FriendlyByteBuf buffer) {
        long epochSecond = buffer.readLong();
        int nano = buffer.readInt();
        if (nano < 0 || nano > 999_999_999) {
            throw new IllegalArgumentException("非法 Instant nano: " + nano);
        }
        return Instant.ofEpochSecond(epochSecond, nano);
    }

    public static void handle(
            ResponseInvestigationViewPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        // DTO 缓存只能在客户端主线程更新，避免 Network Thread 与未来 UI 并发访问。
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ArchiveClientPacketHandler.handleInvestigationViewResponse(packet)
        ));
        context.setPacketHandled(true);
    }

    private static String requireText(String value, String fieldName, int maxLength, boolean allowEmpty) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (!allowEmpty && value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " 长度不能超过 " + maxLength);
        }
        return value;
    }
}
