package top.mores.intelligencearchive.common.casefile.model;

import java.util.List;
import java.util.Set;

/** RequirementSet 列表的 OR 求值工具。空列表表示没有可满足规则。 */
public final class InvestigationRequirements {
    private InvestigationRequirements() {
    }

    public static boolean anySatisfied(
            List<InvestigationRequirementSet> alternatives,
            Set<String> evidenceIds,
            Set<String> clueIds
    ) {
        return alternatives.stream().anyMatch(rule -> rule.isSatisfiedBy(evidenceIds, clueIds));
    }
}
