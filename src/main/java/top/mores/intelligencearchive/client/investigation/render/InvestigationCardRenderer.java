package top.mores.intelligencearchive.client.investigation.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;

/** 统一信息卡片 Renderer；所有 Tab 共享同一套间距、边框与文字层级。 */
public final class InvestigationCardRenderer {
    private static final int CARD_BACKGROUND = 0xEE13242A;
    private static final int CARD_EDGE = 0xFF365B63;
    private static final int TITLE_COLOR = 0xFFE8F4F0;
    private static final int SECURITY_COLOR = 0xFFE37A72;
    private static final int VALUE_COLOR = 0xFFB6C8C5;
    private static final int MUTED_COLOR = 0xFF8BA3A3;
    private static final int SEPARATOR_COLOR = 0x66365B63;
    private static final int PADDING = 8;
    private static final int FIELD_GAP = 6;
    private static final int HEADER_GAP = 5;

    public int measure(Font font, CardContent card, int width) {
        int innerWidth = Math.max(1, width - PADDING * 2);
        int height = PADDING + font.lineHeight;
        if (securityBadgeNeedsOwnLine(font, card, innerWidth)) {
            height += font.lineHeight + 2;
        }
        height += HEADER_GAP + wrappedHeight(font, card.title(), innerWidth) + 7;
        for (CardField field : card.fields()) {
            height += font.lineHeight;
            height += wrappedHeight(font, field.value(), innerWidth) + FIELD_GAP;
        }
        height += 1 + 5 + font.lineHeight;
        return height + PADDING - FIELD_GAP;
    }

    public void render(GuiGraphics graphics, Font font, CardContent card, int x, int y, int width) {
        int height = measure(font, card, width);
        graphics.fill(x, y, x + width, y + height, card.theme().edgeColor());
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, CARD_BACKGROUND);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 3, card.theme().accentColor());

        int innerWidth = Math.max(1, width - PADDING * 2);
        int lineY = y + PADDING;
        String typeBadge = "[" + card.typeLabel() + "]";
        String securityBadge = "[" + card.securityLevel() + "]";
        graphics.drawString(font, typeBadge, x + PADDING, lineY, card.theme().accentColor(), false);

        if (securityBadgeNeedsOwnLine(font, card, innerWidth)) {
            lineY += font.lineHeight + 2;
            graphics.drawString(font, securityBadge, x + PADDING, lineY, SECURITY_COLOR, false);
        } else {
            graphics.drawString(font, securityBadge,
                    x + width - PADDING - font.width(securityBadge), lineY, SECURITY_COLOR, false);
        }
        lineY += font.lineHeight + HEADER_GAP;

        lineY += drawWrapped(graphics, font, card.title(), x + PADDING, lineY, innerWidth, TITLE_COLOR);
        lineY += 7;
        for (CardField field : card.fields()) {
            graphics.drawString(font, field.label() + ":", x + PADDING, lineY,
                    card.theme().accentColor(), false);
            lineY += font.lineHeight;
            lineY += drawWrapped(graphics, font, field.value(), x + PADDING, lineY,
                    innerWidth, VALUE_COLOR) + FIELD_GAP;
        }
        lineY -= FIELD_GAP;
        graphics.fill(x + PADDING, lineY + 1, x + width - PADDING, lineY + 2, SEPARATOR_COLOR);
        lineY += 6;
        graphics.drawString(font, "ID: " + card.id(), x + PADDING, lineY, MUTED_COLOR, false);
    }

    private boolean securityBadgeNeedsOwnLine(Font font, CardContent card, int innerWidth) {
        return font.width("[" + card.typeLabel() + "]")
                + 6
                + font.width("[" + card.securityLevel() + "]") > innerWidth;
    }

    private int wrappedHeight(Font font, String text, int width) {
        return font.split(Component.literal(text), width).size() * font.lineHeight;
    }

    private int drawWrapped(GuiGraphics graphics, Font font, String text, int x, int y, int width, int color) {
        var lines = font.split(Component.literal(text), width);
        for (int index = 0; index < lines.size(); index++) {
            graphics.drawString(font, lines.get(index), x, y + index * font.lineHeight, color, false);
        }
        return lines.size() * font.lineHeight;
    }

    /** 与 Minecraft 绘制 API 无关的卡片内容，便于验证五类信息映射。 */
    public record CardContent(String id, CardTheme theme, String typeLabel, String securityLevel,
                              String title, List<CardField> fields) {
        public CardContent {
            id = Objects.requireNonNull(id, "id 不能为 null");
            theme = Objects.requireNonNull(theme, "theme 不能为 null");
            typeLabel = Objects.requireNonNull(typeLabel, "typeLabel 不能为 null");
            securityLevel = Objects.requireNonNull(securityLevel, "securityLevel 不能为 null");
            title = Objects.requireNonNull(title, "title 不能为 null");
            fields = List.copyOf(Objects.requireNonNull(fields, "fields 不能为 null"));
        }

        public CardContent(String id, String title, List<CardField> fields) {
            this(id, CardTheme.INTEL, "INTEL", "TOP SECRET", title, fields);
        }
    }

    public record CardField(String label, String value) {
        public CardField {
            label = Objects.requireNonNull(label, "label 不能为 null");
            value = Objects.requireNonNull(value, "value 不能为 null");
        }
    }

    public enum CardTheme {
        INTEL(0xFF66C6B4, CARD_EDGE),
        EVIDENCE(0xFFE0B86A, 0xFF6D5C31),
        CLUE(0xFF6EA8E8, 0xFF345A75),
        HYPOTHESIS(0xFFC18CFF, 0xFF5F477A);

        private final int accentColor;
        private final int edgeColor;

        CardTheme(int accentColor, int edgeColor) {
            this.accentColor = accentColor;
            this.edgeColor = edgeColor;
        }

        public int accentColor() {
            return accentColor;
        }

        public int edgeColor() {
            return edgeColor;
        }
    }
}
