package top.mores.intelligencearchive.client.render;

import top.mores.intelligencearchive.client.view.ArchiveViewNode;
import top.mores.intelligencearchive.client.view.ArchiveViewNodeType;
import top.mores.intelligencearchive.client.view.TextViewNode;

/** 普通文本节点：只负责自动换行展示，不解析 Markdown。 */
public final class TextArchiveNodeRenderer implements ArchiveNodeRenderer {
    private static final int COLOR = 0xFFE6E6E6;

    @Override
    public ArchiveViewNodeType type() {
        return ArchiveViewNodeType.TEXT;
    }

    @Override
    public int measure(ArchiveNodeRenderContext context, ArchiveViewNode node, int width) {
        return context.measureWrappedText(asText(node).text(), width);
    }

    @Override
    public void render(ArchiveNodeRenderContext context, ArchiveViewNode node, int x, int y, int width) {
        context.drawWrappedText(asText(node).text(), x, y, width, COLOR);
    }

    private TextViewNode asText(ArchiveViewNode node) {
        if (node instanceof TextViewNode textNode) {
            return textNode;
        }
        throw new IllegalArgumentException("Text renderer 收到了错误的节点类型");
    }
}
