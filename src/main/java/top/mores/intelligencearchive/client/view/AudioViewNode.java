package top.mores.intelligencearchive.client.view;

import java.util.Objects;

public record AudioViewNode(String audioReference) implements ArchiveViewNode {
    public AudioViewNode {
        Objects.requireNonNull(audioReference, "audioReference 不能为 null");
    }

    @Override
    public ArchiveViewNodeType type() {
        return ArchiveViewNodeType.AUDIO;
    }
}
