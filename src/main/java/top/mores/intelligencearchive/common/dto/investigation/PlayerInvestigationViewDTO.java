package top.mores.intelligencearchive.common.dto.investigation;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 服务端向客户端发送的玩家调查视图 DTO。
 *
 * <p>DTO 故意不包含 playerId；客户端身份来自连接对应的 ServerPlayer，不能由包体伪造。</p>
 */
public record PlayerInvestigationViewDTO(
        String caseId,
        String caseTitle,
        String caseStatus,
        List<InvestigationNodeViewDTO> nodes,
        List<InvestigationRelationViewDTO> relations,
        List<InvestigationEvidenceViewDTO> evidence,
        List<InvestigationClueViewDTO> clues,
        List<InvestigationHypothesisViewDTO> hypotheses,
        Instant timestamp
) {
    public static final int MAX_CASE_ID_LENGTH = 128;
    public static final int MAX_CASE_TITLE_LENGTH = 256;
    public static final int MAX_CASE_STATUS_LENGTH = 64;
    public static final int MAX_NODE_COUNT = 100;
    public static final int MAX_RELATION_COUNT = 200;
    public static final int MAX_EVIDENCE_COUNT = 100;
    public static final int MAX_CLUE_COUNT = 100;
    public static final int MAX_HYPOTHESIS_COUNT = 50;

    public PlayerInvestigationViewDTO {
        caseId = InvestigationViewDtoValidation.requireText(caseId, "caseId", MAX_CASE_ID_LENGTH);
        caseTitle = InvestigationViewDtoValidation.requireText(
                caseTitle,
                "caseTitle",
                MAX_CASE_TITLE_LENGTH
        );
        caseStatus = InvestigationViewDtoValidation.requireText(
                caseStatus,
                "caseStatus",
                MAX_CASE_STATUS_LENGTH
        );
        nodes = InvestigationViewDtoValidation.immutableList(nodes, "nodes", MAX_NODE_COUNT);
        relations = InvestigationViewDtoValidation.immutableList(
                relations,
                "relations",
                MAX_RELATION_COUNT
        );
        evidence = InvestigationViewDtoValidation.immutableList(evidence, "evidence", MAX_EVIDENCE_COUNT);
        clues = InvestigationViewDtoValidation.immutableList(clues, "clues", MAX_CLUE_COUNT);
        hypotheses = InvestigationViewDtoValidation.immutableList(
                hypotheses,
                "hypotheses",
                MAX_HYPOTHESIS_COUNT
        );
        timestamp = Objects.requireNonNull(timestamp, "timestamp 不能为 null");
    }
}
