package top.mores.intelligencearchive.client.render;

import top.mores.intelligencearchive.client.view.ArchiveViewNode;
import top.mores.intelligencearchive.client.view.ArchiveViewNodeType;
import top.mores.intelligencearchive.client.view.ImageViewNode;

/** Phase 3-C-2 图片占位渲染器；后续可替换实现而不改 Screen。 */
public final class ImageArchiveNodeRenderer implements ArchiveNodeRenderer {
    private static final int COLOR = 0xFF91B8D8;

    @Override
    public ArchiveViewNodeType type() {
        return ArchiveViewNodeType.IMAGE;
    }

    @Override
    public int measure(ArchiveNodeRenderContext context, ArchiveViewNode node, int width) {
        return context.measureWrappedText(label(node), width);
    }

    @Override
    public void render(ArchiveNodeRenderContext context, ArchiveViewNode node, int x, int y, int width) {
        context.drawWrappedText(label(node), x, y, width, COLOR);
    }

    private String label(ArchiveViewNode node) {
        if (node instanceof ImageViewNode imageNode) {
            return "[IMAGE PLACEHOLDER] " + imageNode.imageReference();
        }
        throw new IllegalArgumentException("Image renderer 收到了错误的节点类型");
    }
}
