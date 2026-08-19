package top.mores.intelligencearchive.common.discovery;

/** 外部调用者可稳定判断的发现结果，不要求解析提示文本或内部 OperationStatus。 */
public enum DiscoveryStatus {
    SUCCESS,
    ALREADY_DISCOVERED,
    NOT_FOUND,
    INVALID_TARGET
}
