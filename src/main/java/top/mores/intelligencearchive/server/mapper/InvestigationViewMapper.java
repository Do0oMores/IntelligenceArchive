package top.mores.intelligencearchive.server.mapper;

import top.mores.intelligencearchive.common.dto.investigation.InvestigationClueViewDTO;
import top.mores.intelligencearchive.common.dto.investigation.InvestigationEvidenceViewDTO;
import top.mores.intelligencearchive.common.dto.investigation.InvestigationHypothesisViewDTO;
import top.mores.intelligencearchive.common.dto.investigation.InvestigationNodeViewDTO;
import top.mores.intelligencearchive.common.dto.investigation.InvestigationRelationViewDTO;
import top.mores.intelligencearchive.common.dto.investigation.PlayerInvestigationViewDTO;
import top.mores.intelligencearchive.common.investigation.view.PlayerInvestigationView;

import java.util.Objects;

/**
 * PlayerInvestigationView 到网络 DTO 的单向映射边界。
 *
 * <p>没有 DTO -> Domain/View 反向入口，客户端因此不能构造服务端业务视图。</p>
 */
public final class InvestigationViewMapper {
    private InvestigationViewMapper() {
    }

    public static PlayerInvestigationViewDTO toDto(PlayerInvestigationView view) {
        Objects.requireNonNull(view, "view 不能为 null");
        return new PlayerInvestigationViewDTO(
                view.caseId(),
                view.caseTitle(),
                view.caseStatus(),
                view.nodes().stream().map(node -> new InvestigationNodeViewDTO(
                        node.intelId(),
                        node.displayName(),
                        node.type().name(),
                        node.discoveryStatus().name(),
                        node.importance()
                )).toList(),
                view.relations().stream().map(relation -> new InvestigationRelationViewDTO(
                        relation.sourceIntelId(),
                        relation.targetIntelId(),
                        relation.relationType().name(),
                        relation.confidence().name(),
                        relation.createdTime()
                )).toList(),
                view.evidence().stream().map(evidence -> new InvestigationEvidenceViewDTO(
                        evidence.evidenceId(),
                        evidence.title(),
                        evidence.sourceType(),
                        evidence.importance()
                )).toList(),
                view.clues().stream().map(clue -> new InvestigationClueViewDTO(
                        clue.clueId(),
                        clue.title(),
                        clue.importance().name(),
                        clue.reliability().name()
                )).toList(),
                view.hypotheses().stream().map(hypothesis -> new InvestigationHypothesisViewDTO(
                        hypothesis.hypothesisId(),
                        hypothesis.title(),
                        hypothesis.status().name(),
                        hypothesis.confidence()
                )).toList(),
                view.generatedAt()
        );
    }
}
