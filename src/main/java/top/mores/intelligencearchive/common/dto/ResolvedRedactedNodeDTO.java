package top.mores.intelligencearchive.common.dto;

import java.util.Objects;

/**
 * 网络传输的服务端打码结果。
 *
 * <p>不包含 conditionReference 或隐藏原文，客户端只能展示服务器给出的状态。</p>
 */
public record ResolvedRedactedNodeDTO(String placeholder, RedactionState state)
        implements ResolvedContentNodeDTO {
    public enum RedactionState {
        REDACTED,
        CONDITION_SATISFIED
    }

    public ResolvedRedactedNodeDTO {
        placeholder = ResolvedContentDtoValidation.requireText(
                placeholder,
                "placeholder",
                ResolvedArchiveContentDTO.MAX_PLACEHOLDER_LENGTH,
                false
        );
        state = Objects.requireNonNull(state, "state 不能为 null");
    }

    @Override
    public ResolvedContentNodeDTOType type() {
        return ResolvedContentNodeDTOType.REDACTED;
    }
}
