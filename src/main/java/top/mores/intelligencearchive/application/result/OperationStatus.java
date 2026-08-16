package top.mores.intelligencearchive.application.result;

/** 应用用例的稳定结果码，调用方不需要解析提示文本来判断业务结果。 */
public enum OperationStatus {
    SUCCESS,
    ALREADY_DISCOVERED,
    INTEL_NOT_FOUND,
    ARCHIVE_NOT_VISIBLE,
    CONTENT_NOT_FOUND,
    CASE_NOT_FOUND,
    EVIDENCE_NOT_FOUND,
    CLUE_NOT_FOUND,
    CLUE_NOT_DISCOVERED,
    HYPOTHESIS_NOT_FOUND,
    HYPOTHESIS_UNAVAILABLE,
    NODE_NOT_FOUND,
    RELATION_ALREADY_EXISTS,
    DEFINITION_CONFLICT,
    INVALID_INPUT,
    INVALID_STATE
}
