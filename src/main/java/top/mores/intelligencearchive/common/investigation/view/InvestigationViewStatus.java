package top.mores.intelligencearchive.common.investigation.view;

/** 玩家已知 Intel 在调查视图中的认知阶段；UNKNOWN 节点不会进入 View。 */
public enum InvestigationViewStatus {
    DISCOVERED,
    READ,
    VERIFIED,
    ARCHIVED
}
