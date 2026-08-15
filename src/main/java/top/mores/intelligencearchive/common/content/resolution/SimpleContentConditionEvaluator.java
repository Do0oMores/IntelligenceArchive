package top.mores.intelligencearchive.common.content.resolution;

import java.util.Objects;

/**
 * Phase 3-C-1 的最小条件实现。
 *
 * <p>条件引用被视为调查状态中的精确认知 ID：玩家发现同名 ID 即满足条件。
 * 本实现不解析前缀或写死具体剧情字符串，未来可整体替换为正式条件服务。</p>
 */
public final class SimpleContentConditionEvaluator implements ContentConditionEvaluator {
    @Override
    public boolean evaluate(String conditionReference, PlayerContentContext context) {
        Objects.requireNonNull(context, "context 不能为 null");
        if (conditionReference == null || conditionReference.isBlank()) {
            return false;
        }
        return context.investigationState().findDiscovery(conditionReference).isPresent();
    }
}
