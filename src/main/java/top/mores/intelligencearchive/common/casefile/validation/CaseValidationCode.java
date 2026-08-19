package top.mores.intelligencearchive.common.casefile.validation;

/** 案件内容校验的稳定问题类别。 */
public enum CaseValidationCode {
    DUPLICATE_ID,
    CASE_ID_MISMATCH,
    THREAD_NOT_FOUND,
    INVALID_REFERENCE,
    INTEL_NODE_NOT_FOUND,
    CLUE_DEPENDENCY_CYCLE,
    EQUIVALENT_FINAL_REQUIREMENTS
}
