package top.mores.intelligencearchive.application.result;

/** 应用用例的稳定结果码，调用方不需要解析提示文本来判断业务结果。 */
public enum OperationStatus {
    SUCCESS,
    ALREADY_DISCOVERED,
    INTEL_NOT_FOUND,
    CONTENT_NOT_FOUND,
    INVALID_INPUT,
    INVALID_STATE
}
