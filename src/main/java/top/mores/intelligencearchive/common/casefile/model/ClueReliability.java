package top.mores.intelligencearchive.common.casefile.model;

/** 内容作者对线索可靠性的叙事分级，避免 0-100 的伪精确数值。 */
public enum ClueReliability {
    UNKNOWN,
    LOW,
    MEDIUM,
    HIGH
}
