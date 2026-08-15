package top.mores.intelligencearchive.client.render;

import top.mores.intelligencearchive.client.view.ArchiveViewNode;
import top.mores.intelligencearchive.client.view.ArchiveViewNodeType;
import top.mores.intelligencearchive.client.view.IntelLinkViewNode;

/** 情报链接占位及点击分派；不在客户端直接改变玩家的情报状态。 */
public final class IntelLinkArchiveNodeRenderer implements ArchiveNodeRenderer {
    private static final int COLOR = 0xFF70C8FF;

    @Override
    public ArchiveViewNodeType type() {
        return ArchiveViewNodeType.INTEL_LINK;
    }

    @Override
    public int measure(ArchiveNodeRenderContext context, ArchiveViewNode node, int width) {
        return context.measureWrappedText(label(node), width);
    }

    @Override
    public void render(ArchiveNodeRenderContext context, ArchiveViewNode node, int x, int y, int width) {
        context.drawWrappedText(label(node), x, y, width, COLOR);
    }

    @Override
    public boolean click(ArchiveViewNode node, ArchiveLinkClickHandler linkClickHandler) {
        linkClickHandler.onIntelLinkClick(asLink(node).targetIntelId());
        return true;
    }

    private String label(ArchiveViewNode node) {
        return "[INTEL LINK] " + asLink(node).targetIntelId();
    }

    private IntelLinkViewNode asLink(ArchiveViewNode node) {
        if (node instanceof IntelLinkViewNode linkNode) {
            return linkNode;
        }
        throw new IllegalArgumentException("IntelLink renderer 收到了错误的节点类型");
    }
}
