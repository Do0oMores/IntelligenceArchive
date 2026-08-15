package top.mores.intelligencearchive.client.view;

import java.util.Objects;

/** 客户端只保留服务端给出的打码结果，不包含条件引用。 */
public record RedactedViewNode(String placeholder, State state) implements ArchiveViewNode {
    public enum State {
        REDACTED,
        CONDITION_SATISFIED
    }

    public RedactedViewNode {
        Objects.requireNonNull(placeholder, "placeholder 不能为 null");
        state = Objects.requireNonNull(state, "state 不能为 null");
    }

    @Override
    public ArchiveViewNodeType type() {
        return ArchiveViewNodeType.REDACTED;
    }
}
