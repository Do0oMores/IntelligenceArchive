package top.mores.intelligencearchive.client.investigation.component;

/**
 * 情报档案终端按钮视觉状态。
 *
 * <p>该类只描述背景、边框、Hover 与 Active 色彩，不承载任何点击、导航或业务逻辑。</p>
 */
public final class ArchiveButtonStyle {

    public static final ArchiveButtonStyle TERMINAL_TAB =
            new ArchiveButtonStyle(
                    ArchiveUiTheme.BUTTON_BACKGROUND,
                    ArchiveUiTheme.BUTTON_HOVER,
                    ArchiveUiTheme.BUTTON_SELECTED,
                    ArchiveUiTheme.BUTTON_BORDER,
                    ArchiveUiTheme.BUTTON_BORDER_ACTIVE,
                    ArchiveUiTheme.MUTED,
                    ArchiveUiTheme.TEXT,
                    ArchiveUiTheme.ACCENT
            );

    private final int backgroundColor;
    private final int hoverBackgroundColor;
    private final int activeBackgroundColor;
    private final int borderColor;
    private final int activeBorderColor;
    private final int textColor;
    private final int hoverTextColor;
    private final int activeTextColor;

    private ArchiveButtonStyle(
            int backgroundColor,
            int hoverBackgroundColor,
            int activeBackgroundColor,
            int borderColor,
            int activeBorderColor,
            int textColor,
            int hoverTextColor,
            int activeTextColor
    ) {
        this.backgroundColor = backgroundColor;
        this.hoverBackgroundColor = hoverBackgroundColor;
        this.activeBackgroundColor = activeBackgroundColor;
        this.borderColor = borderColor;
        this.activeBorderColor = activeBorderColor;
        this.textColor = textColor;
        this.hoverTextColor = hoverTextColor;
        this.activeTextColor = activeTextColor;
    }

    public int backgroundColor(boolean hovered, boolean active) {
        if (active) {
            return activeBackgroundColor;
        }
        if (hovered) {
            return hoverBackgroundColor;
        }
        return backgroundColor;
    }

    public int borderColor(boolean hovered, boolean active) {
        return hovered || active ? activeBorderColor : borderColor;
    }

    public int textColor(boolean hovered, boolean active) {
        if (active) {
            return activeTextColor;
        }
        if (hovered) {
            return hoverTextColor;
        }
        return textColor;
    }
}
