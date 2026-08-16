package top.mores.intelligencearchive.client.render;

/**
 * 节点渲染器使用的最小绘制接口。
 *
 * <p>把 Minecraft GuiGraphics 适配细节隔离在实现类中，使分派与占位渲染保持独立。</p>
 */
public interface ArchiveNodeRenderContext {
    int lineHeight();

    int measureWrappedText(String text, int width);

    void drawWrappedText(String text, int x, int y, int width, int color);
}
