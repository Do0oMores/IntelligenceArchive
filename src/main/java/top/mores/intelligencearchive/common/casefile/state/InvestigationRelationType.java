package top.mores.intelligencearchive.common.casefile.state;

/**
 * 玩家调查图谱可表达的关系类型。
 *
 * <p>该枚举独立于世界侧 IntelRelationType，因为玩家关系允许错误、待证和被推翻。</p>
 */
public enum InvestigationRelationType {
    ASSOCIATED_WITH,
    WORKED_IN,
    SUSPECTED_IN,
    CAUSED,
    SABOTAGED,
    CONTRADICTS,
    SUPPORTS,
    CONNECTED_TO
}
