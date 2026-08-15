package top.mores.intelligencearchive.client.view;

import java.util.Objects;

public record ImageViewNode(String imageReference) implements ArchiveViewNode {
    public ImageViewNode {
        Objects.requireNonNull(imageReference, "imageReference 不能为 null");
    }

    @Override
    public ArchiveViewNodeType type() {
        return ArchiveViewNodeType.IMAGE;
    }
}
