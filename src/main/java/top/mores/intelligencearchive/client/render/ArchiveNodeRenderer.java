package top.mores.intelligencearchive.client.render;

import top.mores.intelligencearchive.client.view.ArchiveViewNode;
import top.mores.intelligencearchive.client.view.ArchiveViewNodeType;

/** 单一节点类型的测量、绘制与可选点击行为。 */
public interface ArchiveNodeRenderer {
    ArchiveViewNodeType type();

    int measure(ArchiveNodeRenderContext context, ArchiveViewNode node, int width);

    void render(ArchiveNodeRenderContext context, ArchiveViewNode node, int x, int y, int width);

    default boolean click(ArchiveViewNode node, ArchiveLinkClickHandler linkClickHandler) {
        return false;
    }
}
