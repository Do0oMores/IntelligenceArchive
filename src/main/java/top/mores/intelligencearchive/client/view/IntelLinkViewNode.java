package top.mores.intelligencearchive.client.view;

import java.util.Objects;

public record IntelLinkViewNode(String targetIntelId) implements ArchiveViewNode {
    public IntelLinkViewNode {
        Objects.requireNonNull(targetIntelId, "targetIntelId 不能为 null");
    }

    @Override
    public ArchiveViewNodeType type() {
        return ArchiveViewNodeType.INTEL_LINK;
    }
}
