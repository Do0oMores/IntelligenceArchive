package top.mores.intelligencearchive.client.investigation.component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;

/**
 * 情报档案终端按钮。
 *
 * <p>
 * 替代 Minecraft 默认 Button，
 * 提供深色终端风格、Hover 高亮以及选中状态。
 * </p>
 */
public class ArchiveButton extends Button {

    private boolean selected;
    private final ArchiveButtonStyle style;


    protected ArchiveButton(
            int x,
            int y,
            int width,
            int height,
            Component message,
            OnPress onPress,
            ArchiveButtonStyle style
    ) {
        super(
                x,
                y,
                width,
                height,
                message,
                onPress,
                DEFAULT_NARRATION
        );
        this.style = style;
    }


    public static Builder archiveBuilder(
            Component message,
            OnPress onPress
    ) {
        return new Builder(message, onPress);
    }


    public ArchiveButton setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }


    public boolean isSelected() {
        return selected;
    }


    @Override
    public void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {

        boolean hovered = this.isHovered();


        int background = style.backgroundColor(hovered, selected);
        int border = style.borderColor(hovered, selected);


        /*
         * 外框
         */
        graphics.fill(
                getX(),
                getY(),
                getX() + width,
                getY() + height,
                border
        );


        /*
         * 内部
         */
        graphics.fill(
                getX() + 1,
                getY() + 1,
                getX() + width - 1,
                getY() + height - 1,
                background
        );


        /*
         * 选中状态左侧扫描条
         */
        if (selected) {

            graphics.fill(
                    getX() + 2,
                    getY() + 3,
                    getX() + 4,
                    getY() + height - 3,
                    ArchiveUiTheme.ACCENT
            );
        }


        /*
         * 文本
         */
        Font font = Minecraft.getInstance().font;

        graphics.drawCenteredString(
                font,
                getMessage(),
                getX() + width / 2,
                getY() + (height - 8) / 2,
                style.textColor(hovered, selected)
        );
    }


    @Override
    public void updateWidgetNarration(
            NarrationElementOutput output
    ) {
        defaultButtonNarrationText(output);
    }


    public static class Builder {

        private final Component message;
        private final OnPress onPress;

        private int x;
        private int y;
        private int width = 150;
        private int height = 24;
        private ArchiveButtonStyle style = ArchiveButtonStyle.TERMINAL_TAB;


        private Builder(
                Component message,
                OnPress onPress
        ) {
            this.message = message;
            this.onPress = onPress;
        }


        public Builder bounds(
                int x,
                int y,
                int width,
                int height
        ) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }


        public Builder style(ArchiveButtonStyle style) {
            this.style = style;
            return this;
        }


        public ArchiveButton build() {

            return new ArchiveButton(
                    x,
                    y,
                    width,
                    height,
                    message,
                    onPress,
                    style
            );
        }
    }
}
