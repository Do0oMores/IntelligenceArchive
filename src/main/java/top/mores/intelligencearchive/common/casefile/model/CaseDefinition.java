package top.mores.intelligencearchive.common.casefile.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 可共享的静态案件内容定义。
 *
 * <p>这里绝不保存 playerId、当前阶段或已发现内容；那些字段属于每位玩家独立的调查状态。</p>
 */
public record CaseDefinition(
        String id,
        String title,
        String description,
        CaseDifficulty difficulty,
        List<InvestigationThreadDefinition> threads,
        List<EvidenceDefinition> evidence,
        List<ClueDefinition> clues,
        List<HypothesisDefinition> hypotheses,
        Set<String> relatedIntelNodeIds
) {
    public CaseDefinition {
        id = CaseModelValidation.requireId(id, "id");
        title = CaseModelValidation.requireId(title, "title");
        description = CaseModelValidation.requireText(description, "description");
        difficulty = Objects.requireNonNull(difficulty, "difficulty 不能为 null");
        threads = CaseModelValidation.immutableList(threads, "threads");
        evidence = CaseModelValidation.immutableList(evidence, "evidence");
        clues = CaseModelValidation.immutableList(clues, "clues");
        hypotheses = CaseModelValidation.immutableList(hypotheses, "hypotheses");
        relatedIntelNodeIds = CaseModelValidation.immutableIds(relatedIntelNodeIds, "relatedIntelNodeIds");
    }

    public Optional<EvidenceDefinition> findEvidence(String evidenceId) {
        return evidence.stream().filter(value -> value.id().equals(evidenceId)).findFirst();
    }

    public Optional<ClueDefinition> findClue(String clueId) {
        return clues.stream().filter(value -> value.id().equals(clueId)).findFirst();
    }

    public Optional<HypothesisDefinition> findHypothesis(String hypothesisId) {
        return hypotheses.stream().filter(value -> value.id().equals(hypothesisId)).findFirst();
    }
}
