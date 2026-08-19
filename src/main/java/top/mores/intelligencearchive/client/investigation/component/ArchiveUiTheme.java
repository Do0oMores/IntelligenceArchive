package top.mores.intelligencearchive.client.investigation.component;

/**
 * Investigation Terminal 统一 UI 主题。
 *
 * <p>所有调查终端 GUI 组件禁止自行定义颜色，统一从这里读取。</p>
 */
public final class ArchiveUiTheme {

    private ArchiveUiTheme() {
    }

    /*
     * 基础背景
     */
    public static final int BACKGROUND =
            0xFF061014;


    /*
     * 面板
     */
    public static final int PANEL =
            0xF00D1B20;

    public static final int PANEL_EDGE =
            0xFF31515A;


    /*
     * 卡片
     */
    public static final int CARD_BACKGROUND =
            0xEE13242A;

    public static final int CARD_EDGE =
            0xFF365B63;


    /*
     * 文字
     */
    public static final int TEXT =
            0xFFE8F4F0;

    public static final int MUTED =
            0xFF8BA3A3;


    /*
     * 强调
     */
    public static final int ACCENT =
            0xFF66C6B4;

    public static final int GRID =
            0x142E454A;


    /*
     * 数值
     */
    public static final int VALUE =
            0xFFB6C8C5;


    /*
     * 状态
     */
    public static final int WARNING =
            0xFFE0B86A;

    public static final int DANGER =
            0xFFE37A72;

    public static final int ERROR =
            DANGER;

    public static final int HIDDEN =
            0xFF303638;

    public static final int LOCKED =
            0xFF555555;


    /*
     * Button
     */

    public static final int BUTTON_BACKGROUND =
            0xCC13242A;

    public static final int BUTTON_HOVER =
            0xDD19343B;

    public static final int BUTTON_SELECTED =
            0xEE164047;

    public static final int BUTTON_BORDER =
            0xFF365B63;

    public static final int BUTTON_BORDER_ACTIVE =
            ACCENT;
}
