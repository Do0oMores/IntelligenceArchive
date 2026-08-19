package top.mores.intelligencearchive.common.casefile.validation;

import java.util.List;
import java.util.Objects;

/** 一次 CaseDefinition 静态校验的不可变结果。 */
public record CaseValidationResult(List<CaseValidationIssue> issues) {
    public CaseValidationResult {
        issues = List.copyOf(Objects.requireNonNull(issues, "issues 不能为 null"));
    }

    public boolean valid() {
        return issues.isEmpty();
    }
}
