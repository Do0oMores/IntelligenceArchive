package top.mores.intelligencearchive.client.render;

import top.mores.intelligencearchive.client.view.ArchiveViewNode;
import top.mores.intelligencearchive.client.view.ArchiveViewNodeType;
import top.mores.intelligencearchive.client.view.AudioViewNode;

/** Phase 3-C-2 音频按钮式占位，不加载也不播放音频资源。 */
public final class AudioArchiveNodeRenderer implements ArchiveNodeRenderer {
    private static final int COLOR = 0xFFC7A7E8;

    @Override
    public ArchiveViewNodeType type() {
        return ArchiveViewNodeType.AUDIO;
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
        if (node instanceof AudioViewNode audioNode) {
            return "[AUDIO] > " + audioNode.audioReference();
        }
        throw new IllegalArgumentException("Audio renderer 收到了错误的节点类型");
    }
}
