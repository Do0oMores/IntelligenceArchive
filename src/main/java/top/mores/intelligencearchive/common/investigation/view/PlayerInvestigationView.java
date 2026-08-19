package top.mores.intelligencearchive.common.investigation.view;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 某位玩家在单个 Case 中的完整认知投影，而不是服务器持有的世界真相。
 *
 * <p>所有集合均防御性复制，未来 DTO/客户端只能消费该快照，不能借此修改服务端状态。</p>
 */
public record PlayerInvestigationView(
        UUID playerId,
        String caseId,
        String caseTitle,
        String caseStatus,
        List<InvestigationNodeView> nodes,
        List<InvestigationRelationView> relations,
        List<InvestigationEvidenceView> evidence,
        List<InvestigationClueView> clues,
        List<InvestigationHypothesisView> hypotheses,
        Instant generatedAt
) {
    public PlayerInvestigationView {
        playerId = Objects.requireNonNull(playerId, "playerId 不能为 null");
        caseId = InvestigationViewValidation.requireText(caseId, "caseId");
        caseTitle = InvestigationViewValidation.requireText(caseTitle, "caseTitle");
        caseStatus = InvestigationViewValidation.requireText(caseStatus, "caseStatus");
        nodes = List.copyOf(Objects.requireNonNull(nodes, "nodes 不能为 null"));
        relations = List.copyOf(Objects.requireNonNull(relations, "relations 不能为 null"));
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence 不能为 null"));
        clues = List.copyOf(Objects.requireNonNull(clues, "clues 不能为 null"));
        hypotheses = List.copyOf(Objects.requireNonNull(hypotheses, "hypotheses 不能为 null"));
        generatedAt = Objects.requireNonNull(generatedAt, "generatedAt 不能为 null");
    }
}
