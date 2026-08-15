package top.mores.intelligencearchive.common.model;

/** 档案的表现类型；UNKNOWN 用于安全承接尚未识别或未来新增的类型。 */
public enum ArchiveDocumentType {
    DOCUMENT,
    IMAGE,
    AUDIO,
    REPORT,
    TRANSCRIPT,
    UNKNOWN
}
