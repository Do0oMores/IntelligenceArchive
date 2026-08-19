package top.mores.intelligencearchive.common.casefile.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 从 Evidence 或既有 Clue 派生出的调查认知。
 *
 * <p>派生线索只授予认知，不会自动在玩家图谱中创建关系。</p>
 */
public record ClueDefinition(
        String id,
        String caseId,
        String threadId,
        String title,
        String description,
        List<InvestigationRequirementSet> derivationRules,
        ClueReliability reliability,
        ClueImportance importance,
        Set<String> relatedIntelNodeIds,
        Set<String> supportsHypothesisIds,
        Set<String> contradictsHypothesisIds
) {
    public ClueDefinition {
        id = CaseModelValidation.requireId(id, "id");
        caseId = CaseModelValidation.requireId(caseId, "caseId");
        threadId = CaseModelValidation.requireId(threadId, "threadId");
        title = CaseModelValidation.requireId(title, "title");
        description = CaseModelValidation.requireText(description, "description");
        derivationRules = CaseModelValidation.immutableList(derivationRules, "derivationRules");
        reliability = Objects.requireNonNull(reliability, "reliability 不能为 null");
        importance = Objects.requireNonNull(importance, "importance 不能为 null");
        relatedIntelNodeIds = CaseModelValidation.immutableIds(relatedIntelNodeIds, "relatedIntelNodeIds");
        supportsHypothesisIds = CaseModelValidation.immutableIds(supportsHypothesisIds, "supportsHypothesisIds");
        contradictsHypothesisIds = CaseModelValidation.immutableIds(contradictsHypothesisIds, "contradictsHypothesisIds");
    }
}
