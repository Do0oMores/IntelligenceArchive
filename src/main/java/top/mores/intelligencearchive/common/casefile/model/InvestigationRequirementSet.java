package top.mores.intelligencearchive.common.casefile.model;

import java.util.Set;

/**
 * 一个简单的调查条件组。
 *
 * <p>组内所有 Evidence 与 Clue 都必须存在，因此组内语义为 AND；拥有多个本对象的
 * 规则列表由调用方按 OR 处理。本阶段刻意不提供脚本、NOT 或嵌套表达式。</p>
 */
public record InvestigationRequirementSet(
        Set<String> requiredEvidenceIds,
        Set<String> requiredClueIds
) {
    public InvestigationRequirementSet {
        requiredEvidenceIds = CaseModelValidation.immutableIds(requiredEvidenceIds, "requiredEvidenceIds");
        requiredClueIds = CaseModelValidation.immutableIds(requiredClueIds, "requiredClueIds");
        if (requiredEvidenceIds.isEmpty() && requiredClueIds.isEmpty()) {
            throw new IllegalArgumentException("RequirementSet 至少需要一个 Evidence 或 Clue");
        }
    }

    public boolean isSatisfiedBy(Set<String> evidenceIds, Set<String> clueIds) {
        return evidenceIds.containsAll(requiredEvidenceIds) && clueIds.containsAll(requiredClueIds);
    }
}
