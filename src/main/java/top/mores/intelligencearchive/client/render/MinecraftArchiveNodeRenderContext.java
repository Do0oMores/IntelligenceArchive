package top.mores.intelligencearchive.client.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Objects;

/** ArchiveNodeRenderContext 的 Minecraft 1.20.1 GUI 适配器。 */
public final class MinecraftArchiveNodeRenderContext implements ArchiveNodeRenderContext {
    private static final int LINE_GAP = 2;

    private final GuiGraphics graphics;
    private final Font font;

    public MinecraftArchiveNodeRenderContext(GuiGraphics graphics, Font font) {
        this.graphics = Objects.requireNonNull(graphics, "graphics 不能为 null");
        this.font = Objects.requireNonNull(font, "font 不能为 null");
    }

    @Override
    public int lineHeight() {
        return font.lineHeight + LINE_GAP;
    }

    @Override
    public int measureWrappedText(String text, int width) {
        return Math.max(1, split(text, width).size()) * lineHeight();
    }

    @Override
    public void drawWrappedText(String text, int x, int y, int width, int color) {
        int lineY = y;
        for (FormattedCharSequence line : split(text, width)) {
            graphics.drawString(font, line, x, lineY, color, false);
            lineY += lineHeight();
        }
    }

    private List<FormattedCharSequence> split(String text, int width) {
        return font.split(Component.literal(text), Math.max(1, width));
    }
}
