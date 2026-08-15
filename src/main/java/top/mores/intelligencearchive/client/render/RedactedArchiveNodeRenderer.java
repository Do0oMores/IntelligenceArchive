package top.mores.intelligencearchive.client.render;

import top.mores.intelligencearchive.client.view.ArchiveViewNode;
import top.mores.intelligencearchive.client.view.ArchiveViewNodeType;
import top.mores.intelligencearchive.client.view.RedactedViewNode;

/**
 * 打码节点展示器。
 *
 * <p>这里只显示服务端给出的最终状态，不含条件表达式，也不在客户端重新判断权限。</p>
 */
public final class RedactedArchiveNodeRenderer implements ArchiveNodeRenderer {
    private static final int COLOR = 0xFFFF8C8C;

    @Override
    public ArchiveViewNodeType type() {
        return ArchiveViewNodeType.REDACTED;
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
        if (!(node instanceof RedactedViewNode redactedNode)) {
            throw new IllegalArgumentException("Redacted renderer 收到了错误的节点类型");
        }
        String suffix = redactedNode.state() == RedactedViewNode.State.CONDITION_SATISFIED
                ? " [SERVER STATE: CONDITION SATISFIED]"
                : "";
        return redactedNode.placeholder() + suffix;
    }
}
