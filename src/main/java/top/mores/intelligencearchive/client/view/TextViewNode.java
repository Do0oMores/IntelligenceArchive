package top.mores.intelligencearchive.client.view;

import java.util.Objects;

public record TextViewNode(String text) implements ArchiveViewNode {
    public TextViewNode {
        Objects.requireNonNull(text, "text 不能为 null");
    }

    @Override
    public ArchiveViewNodeType type() {
        return ArchiveViewNodeType.TEXT;
    }
}
