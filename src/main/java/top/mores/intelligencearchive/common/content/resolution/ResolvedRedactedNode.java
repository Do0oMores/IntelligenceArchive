package top.mores.intelligencearchive.common.content.resolution;

import java.util.Objects;

/**
 * 玩家视角的安全打码节点。
 *
 * <p>即使条件满足，本阶段也只有占位符，因为 Phase 3-B 已安全丢弃隐藏原文。
 * {@code CONDITION_SATISFIED} 只表示服务器未来应选择另一授权内容版本，不携带秘密或条件 ID。</p>
 */
public record ResolvedRedactedNode(
        String placeholder,
        ResolvedRedactionState state
) implements ResolvedContentNode {
    public ResolvedRedactedNode {
        placeholder = ResolutionValidation.requireNonBlank(placeholder, "placeholder");
        state = Objects.requireNonNull(state, "state 不能为 null");
    }

    @Override
    public ResolvedContentNodeType type() {
        return ResolvedContentNodeType.REDACTED;
    }
}
