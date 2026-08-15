package top.mores.intelligencearchive.common.model.investigation;

/**
 * 玩家对单个情报对象的认知阶段。
 *
 * <p>枚举顺序代表认知进度，但业务代码通过 {@link #canAdvanceTo(IntelDiscoveryStatus)}
 * 表达升级规则，避免调用方直接依赖 {@code ordinal()}。</p>
 */
public enum IntelDiscoveryStatus {
    UNKNOWN(0),
    DISCOVERED(1),
    READ(2),
    VERIFIED(3),
    ARCHIVED(4);

    private final int progression;

    IntelDiscoveryStatus(int progression) {
        this.progression = progression;
    }

    /** 调查认知只能保持或前进，不能通过普通更新接口回退。 */
    public boolean canAdvanceTo(IntelDiscoveryStatus target) {
        return target != null && target.progression >= progression;
    }
}
