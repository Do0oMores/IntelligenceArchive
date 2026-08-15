package top.mores.intelligencearchive.common.model;

/** 档案的基础安全等级；它描述档案元数据，不直接代表某个玩家是否有权访问。 */
public enum ArchiveSecurityLevel {
    PUBLIC,
    RESTRICTED,
    SECRET,
    CLASSIFIED
}
