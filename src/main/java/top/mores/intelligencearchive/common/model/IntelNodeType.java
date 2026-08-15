package top.mores.intelligencearchive.common.model;

/** 调查图谱节点的基础类型，避免由任意字符串承担核心分类语义。 */
public enum IntelNodeType {
    PERSON,
    LOCATION,
    EVENT,
    ITEM,
    ORGANIZATION,
    CONCEPT,
    UNKNOWN
}
