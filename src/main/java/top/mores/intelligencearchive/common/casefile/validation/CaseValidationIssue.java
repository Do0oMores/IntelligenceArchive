package top.mores.intelligencearchive.common.casefile.validation;

import java.util.Objects;

/** 单条可供内容制作工具展示的校验问题。 */
public record CaseValidationIssue(CaseValidationCode code, String subjectId, String message) {
    public CaseValidationIssue {
        code = Objects.requireNonNull(code, "code 不能为 null");
        subjectId = Objects.requireNonNull(subjectId, "subjectId 不能为 null");
        message = Objects.requireNonNull(message, "message 不能为 null");
    }
}
