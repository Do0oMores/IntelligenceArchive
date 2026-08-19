package top.mores.intelligencearchive.client.investigation.render;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import top.mores.intelligencearchive.client.investigation.InvestigationScreenState;
import top.mores.intelligencearchive.client.investigation.InvestigationTab;
import top.mores.intelligencearchive.client.investigation.InvestigationViewLoadStatus;
import top.mores.intelligencearchive.client.investigation.component.ArchiveUiTheme;
import top.mores.intelligencearchive.client.investigation.view.ClueCardViewModel;
import top.mores.intelligencearchive.client.investigation.view.EvidenceCardViewModel;
import top.mores.intelligencearchive.client.investigation.view.HypothesisCardViewModel;
import top.mores.intelligencearchive.client.investigation.view.IntelCardViewModel;
import top.mores.intelligencearchive.client.investigation.view.InvestigationViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Investigation Terminal 的信息布局层。
 *
 * <p>该 Renderer 只组织案件摘要、Tab 与统一信息卡片；不读取 DTO、不推理业务状态，
 * 也没有任何图布局、关系连线、拖拽或缩放逻辑。</p>
 */
public final class InvestigationScreenRenderer {
    private static final int BACKGROUND = ArchiveUiTheme.BACKGROUND;
    private static final int PANEL = ArchiveUiTheme.PANEL;
    private static final int PANEL_EDGE = ArchiveUiTheme.PANEL_EDGE;
    private static final int TEXT = ArchiveUiTheme.TEXT;
    private static final int MUTED = ArchiveUiTheme.MUTED;
    private static final int ACCENT = ArchiveUiTheme.ACCENT;
    private static final int WARNING = ArchiveUiTheme.WARNING;
    private static final int ERROR = ArchiveUiTheme.ERROR;
    private static final int CARD_GAP = 8;
    private static final int LOCKED = ArchiveUiTheme.LOCKED;
    private static final int HIDDEN = ArchiveUiTheme.HIDDEN;
    private static final int GRID = ArchiveUiTheme.GRID;
    private static final String DEFAULT_SECURITY_LEVEL = "TOP SECRET";

    private final InvestigationCardRenderer cardRenderer = new InvestigationCardRenderer();

    public Layout layout(int width, int height) {
        int margin = 12;
        int headerBottom = 39;
        int footerTop = Math.max(headerBottom + 80, height - 25);
        int navigationWidth = Math.min(116, Math.max(86, width / 5));
        int statusWidth = Math.min(166, Math.max(116, width / 4));
        int contentLeft = margin + navigationWidth + 8;
        int statusLeft = Math.max(contentLeft + 130, width - margin - statusWidth);
        return new Layout(margin, contentLeft - 8, contentLeft, statusLeft - 8,
                statusLeft, width - margin, headerBottom, footerTop);
    }

    public RenderMetrics render(
            GuiGraphics graphics,
            Font font,
            int width,
            int height,
            InvestigationScreenState.Snapshot state,
            double scrollOffset
    ) {
        Layout layout = layout(width, height);
        graphics.fill(0, 0, width, height, BACKGROUND);
        drawScanLines(graphics, width, height);
        graphics.drawCenteredString(font, terminalTitle(), width / 2, 9, TEXT);
        graphics.drawCenteredString(font, Component.translatable(
                "gui.intelligencearchive.investigation.subtitle"
        ), width / 2, 22, MUTED);

        drawPanel(graphics, font, layout.navigationLeft(), layout.headerBottom(), layout.navigationRight(),
                layout.footerTop(), "gui.intelligencearchive.investigation.panel.navigation");
        drawPanel(graphics, font, layout.contentLeft(), layout.headerBottom(), layout.contentRight(),
                layout.footerTop(), "gui.intelligencearchive.investigation.panel.database");
        drawPanel(graphics, font, layout.statusLeft(), layout.headerBottom(), layout.statusRight(),
                layout.footerTop(), "gui.intelligencearchive.investigation.panel.system");

        RenderMetrics metrics = drawContent(graphics, font, layout, state, scrollOffset);
        drawSystemStatus(graphics, font, layout, state);
        drawFooter(graphics, font, height, state.loadStatus());
        drawScrollbar(graphics, layout, metrics, scrollOffset);
        return metrics;
    }

    private Component terminalTitle() {
        String title = Component.translatable(
                "gui.intelligencearchive.investigation.title"
        ).getString();
        if ((System.currentTimeMillis() / 500L) % 2L == 0L) {
            title += "_";
        }
        return Component.literal(title);
    }

    private void drawScanLines(
            GuiGraphics graphics,
            int width,
            int height
    ){
        for(int y = 0; y < height; y += 3){
            graphics.fill(
                    0,
                    y,
                    width,
                    y + 1,
                    0x08000000
            );
        }
    }

    private RenderMetrics drawContent(
            GuiGraphics graphics,
            Font font,
            Layout layout,
            InvestigationScreenState.Snapshot state,
            double scrollOffset
    ) {
        int left = layout.contentLeft() + 8;
        int right = layout.contentRight() - 9;
        int viewportTop = layout.headerBottom() + 48;
        int viewportBottom = layout.footerTop() - 7;
        int viewportHeight = Math.max(0, viewportBottom - viewportTop);
        graphics.drawString(font, state.selectedTab().displayName(), left, layout.headerBottom() + 29, ACCENT, false);

        String statusMessage = statusMessage(state);
        if (statusMessage != null) {
            int color = state.loadStatus() == InvestigationViewLoadStatus.FAILED ? ERROR : MUTED;
            drawWrapped(graphics, font, statusMessage, left, viewportTop + 4, Math.max(1, right - left), color);
            return new RenderMetrics(0, viewportHeight, left, viewportTop, right, viewportBottom);
        }

        InvestigationViewModel view = state.viewModel().orElseThrow();
        List<InvestigationCardRenderer.CardContent> cards = cardsFor(state.selectedTab(), view);
        if (cards.isEmpty()) {
            graphics.drawString(
                    font,
                    Component.translatable(
                            "gui.intelligencearchive.investigation.no_records"
                    ),
                    left,
                    viewportTop + 4,
                    MUTED,
                    false
            );
            return new RenderMetrics(0, viewportHeight, left, viewportTop, right, viewportBottom);
        }

        int availableWidth = Math.max(80, right - left);
        int columns = availableWidth >= 350 ? 2 : 1;
        int cardWidth = Math.max(70, (availableWidth - CARD_GAP * (columns - 1)) / columns);
        List<CardPlacement> placements = placeCards(font, cards, columns, cardWidth);
        int contentHeight = placements.stream().mapToInt(value -> value.y() + value.height()).max().orElse(0);

        graphics.enableScissor(layout.contentLeft() + 1, viewportTop, layout.contentRight() - 1, viewportBottom);
        for (CardPlacement placement : placements) {
            int drawY = viewportTop + placement.y() - (int) scrollOffset;
            if (drawY + placement.height() < viewportTop || drawY >= viewportBottom) {
                continue;
            }
            int drawX = left + placement.column() * (cardWidth + CARD_GAP);
            cardRenderer.render(graphics, font, placement.card(), drawX, drawY, cardWidth);
        }
        graphics.disableScissor();
        return new RenderMetrics(contentHeight, viewportHeight, left, viewportTop, right, viewportBottom);
    }

    private List<CardPlacement> placeCards(Font font, List<InvestigationCardRenderer.CardContent> cards,
                                            int columns, int cardWidth) {
        List<CardPlacement> placements = new ArrayList<>(cards.size());
        int y = 0;
        for (int start = 0; start < cards.size(); start += columns) {
            int rowEnd = Math.min(cards.size(), start + columns);
            int rowHeight = 0;
            for (int index = start; index < rowEnd; index++) {
                rowHeight = Math.max(rowHeight, cardRenderer.measure(font, cards.get(index), cardWidth));
            }
            for (int index = start; index < rowEnd; index++) {
                InvestigationCardRenderer.CardContent card = cards.get(index);
                placements.add(new CardPlacement(card, index - start, y,
                        cardRenderer.measure(font, card, cardWidth)));
            }
            y += rowHeight + CARD_GAP;
        }
        return List.copyOf(placements);
    }

    /** 将五类 ViewModel 转成统一卡片内容；不访问 DTO 或领域定义。 */
    public List<InvestigationCardRenderer.CardContent> cardsFor(
            InvestigationTab tab,
            InvestigationViewModel view
    ) {
        return switch (tab) {
            case CASE -> List.of(
                    new InvestigationCardRenderer.CardContent(
                            view.caseSummary().caseId(),
                            InvestigationCardRenderer.CardTheme.INTEL,
                            "CASE",
                            DEFAULT_SECURITY_LEVEL,
                            view.caseSummary().title(),
                            List.of(
                                    field("gui.intelligencearchive.investigation.field.case_id",
                                            view.caseSummary().caseId()),

                                    field("gui.intelligencearchive.investigation.field.status",
                                            view.caseSummary().status()),

                                    field("gui.intelligencearchive.investigation.field.known_intel",
                                            view.intelCards().size()),

                                    field("gui.intelligencearchive.investigation.field.evidence",
                                            view.evidenceCards().size()),

                                    field("gui.intelligencearchive.investigation.field.clues",
                                            view.clueCards().size()),

                                    field("gui.intelligencearchive.investigation.field.hypotheses",
                                            view.hypothesisCards().size())
                            )
                    )
            );
            case INTEL ->
                    view.intelCards()
                            .stream()
                            .map(this::intelCard)
                            .toList();


            case EVIDENCE ->
                    view.evidenceCards()
                            .stream()
                            .map(this::evidenceCard)
                            .toList();


            case CLUES ->
                    view.clueCards()
                            .stream()
                            .map(this::clueCard)
                            .toList();


            case HYPOTHESIS ->
                    view.hypothesisCards()
                            .stream()
                            .map(this::hypothesisCard)
                            .toList();
        };
    }

    private InvestigationCardRenderer.CardContent intelCard(
            IntelCardViewModel item
    ) {
        return new InvestigationCardRenderer.CardContent(
                item.id(),
                InvestigationCardRenderer.CardTheme.INTEL,
                "INTEL",
                DEFAULT_SECURITY_LEVEL,
                item.title(),
                List.of(
                        field(
                                "gui.intelligencearchive.investigation.field.category",
                                item.category()
                        ),

                        field(
                                "gui.intelligencearchive.investigation.field.importance",
                                item.importance()
                        )
                )
        );
    }

    private InvestigationCardRenderer.CardContent evidenceCard(
            EvidenceCardViewModel item
    ) {
        return new InvestigationCardRenderer.CardContent(
                item.id(),
                InvestigationCardRenderer.CardTheme.EVIDENCE,
                "EVIDENCE",
                DEFAULT_SECURITY_LEVEL,
                item.title(),
                List.of(

                        field(
                                "gui.intelligencearchive.investigation.field.source",
                                item.sourceType()
                        ),

                        field(
                                "gui.intelligencearchive.investigation.field.importance",
                                item.importance()
                        ),

                        field(
                                "gui.intelligencearchive.investigation.field.status",
                                Component.translatable(
                                        "gui.intelligencearchive.investigation.status.discovered"
                                ).getString()
                        )
                )
        );
    }

    private InvestigationCardRenderer.CardContent clueCard(
            ClueCardViewModel item
    ) {
        return new InvestigationCardRenderer.CardContent(
                item.id(),
                InvestigationCardRenderer.CardTheme.CLUE,
                "CLUE",
                DEFAULT_SECURITY_LEVEL,
                item.title(),
                List.of(
                        field(
                                "gui.intelligencearchive.investigation.field.reliability",
                                item.reliability()
                        ),

                        field(
                                "gui.intelligencearchive.investigation.field.importance",
                                item.importance()
                        )
                )
        );
    }

    private InvestigationCardRenderer.CardContent hypothesisCard(
            HypothesisCardViewModel item
    ) {
        return new InvestigationCardRenderer.CardContent(
                item.id(),
                InvestigationCardRenderer.CardTheme.HYPOTHESIS,
                "HYPOTHESIS",
                DEFAULT_SECURITY_LEVEL,
                item.title(),
                List.of(
                        field(
                                "gui.intelligencearchive.investigation.field.current_assessment",
                                item.status()
                        ),

                        field(
                                "gui.intelligencearchive.investigation.field.confidence",
                                item.confidence()
                        )
                )
        );
    }

    private InvestigationCardRenderer.CardField field(
            String key,
            Object value
    ) {
        return new InvestigationCardRenderer.CardField(
                Component.translatable(key).getString(),
                String.valueOf(value)
        );
    }

    private void drawSystemStatus(GuiGraphics graphics, Font font, Layout layout,
                                  InvestigationScreenState.Snapshot state) {
        int x = layout.statusLeft() + 8;
        int y = layout.headerBottom() + 30;
        int width = Math.max(40, layout.statusRight() - x - 8);

        InvestigationViewModel view = state.viewModel().orElse(null);
        if (view == null) {
            drawHudEntry(
                    graphics,
                    font,
                    x,
                    y,
                    "CASE:",
                    state.currentCaseId().isBlank()
                            ? "NO CASE"
                            : state.currentCaseId(),
                    width,
                    MUTED
            );
            y += 31;
            drawHudEntry(graphics, font, x, y, "SECURITY:", "LOCKED", width, LOCKED);
            y += 31;
            drawHudEntry(graphics, font, x, y, "SYNC:", syncLabel(state.loadStatus()), width,
                    syncColor(state.loadStatus()));
            y += 31;
            drawHudEntry(graphics, font, x, y, "DATA:", "0%", width, HIDDEN);
            return;
        }

        drawHudEntry(graphics, font, x, y, "CASE:", view.caseSummary().caseId(), width, TEXT);
        y += 31;
        drawHudEntry(graphics, font, x, y, "SECURITY:", "LEVEL 3", width, ERROR);
        y += 31;
        drawHudEntry(graphics, font, x, y, "SYNC:", syncLabel(state.loadStatus()), width,
                syncColor(state.loadStatus()));
        y += 31;
        drawHudEntry(graphics, font, x, y, "DATA:", dataPercent(view) + "%", width, ACCENT);
        y += 35;

        graphics.drawString(font, Component.translatable(
                "gui.intelligencearchive.investigation.records"
        ), x, y, ACCENT, false);
        y += 13;
        graphics.drawString(font, "INTEL  " + view.intelCards().size(), x, y, TEXT, false);
        y += 11;
        graphics.drawString(font, "EVID.  " + view.evidenceCards().size(), x, y, WARNING, false);
        y += 11;
        graphics.drawString(font, "CLUES  " + view.clueCards().size(), x, y, 0xFF6EA8E8, false);
        y += 11;
        graphics.drawString(font, "HYP.   " + view.hypothesisCards().size(), x, y, 0xFFC18CFF, false);
        y += 17;

        int barLeft = x;
        int barRight = x + Math.max(28, width);
        int filledRight = barLeft + (barRight - barLeft) * dataPercent(view) / 100;
        graphics.fill(barLeft, y, barRight, y + 4, HIDDEN);
        graphics.fill(barLeft, y, filledRight, y + 4, ACCENT);
    }

    private void drawHudEntry(GuiGraphics graphics, Font font, int x, int y,
                              String label, String value, int width, int valueColor) {
        graphics.drawString(font, label, x, y, ACCENT, false);
        drawWrapped(graphics, font, value, x, y + 12, width, valueColor);
    }

    private String syncLabel(InvestigationViewLoadStatus status) {
        return switch (status) {
            case LOADED -> "ONLINE";
            case REQUESTING -> "SYNCING";
            case FAILED -> "OFFLINE";
            case IDLE -> "STANDBY";
        };
    }

    private int syncColor(InvestigationViewLoadStatus status) {
        return switch (status) {
            case LOADED -> ACCENT;
            case REQUESTING -> WARNING;
            case FAILED -> ERROR;
            case IDLE -> MUTED;
        };
    }

    private int dataPercent(InvestigationViewModel view) {
        int records = view.intelCards().size()
                + view.evidenceCards().size()
                + view.clueCards().size()
                + view.hypothesisCards().size();
        return Math.min(99, Math.max(12, records * 7));
    }

    private String statusMessage(
            InvestigationScreenState.Snapshot state
    ) {
        return switch(state.loadStatus()) {

            case IDLE ->
                    Component.translatable(
                            "gui.intelligencearchive.investigation.status.idle"
                    ).getString();


            case REQUESTING ->
                    animatedRequestMessage();


            case FAILED ->
                    Component.translatable(
                            "gui.intelligencearchive.investigation.status.failed",
                            state.errorMessage()
                    ).getString();


            case LOADED ->
                    state.viewModel().isEmpty()
                            ?
                            Component.translatable(
                                    "gui.intelligencearchive.investigation.status.empty"
                            ).getString()
                            :
                            null;
        };
    }

    private void drawFooter(GuiGraphics graphics, Font font, int height, InvestigationViewLoadStatus status) {
        Component text =
                Component.translatable(
                        "gui.intelligencearchive.investigation.footer.status",
                        switch(status){

                            case IDLE ->
                                    Component.translatable(
                                            "gui.intelligencearchive.investigation.footer.idle"
                                    );

                            case REQUESTING ->
                                    Component.translatable(
                                            "gui.intelligencearchive.investigation.footer.requesting"
                                    );

                            case LOADED ->
                                    Component.translatable(
                                            "gui.intelligencearchive.investigation.footer.connected"
                                    );

                            case FAILED ->
                                    Component.translatable(
                                            "gui.intelligencearchive.investigation.footer.failed"
                                    );
                        }
                );
        graphics.drawString(font, text, 12, height - 16,
                status == InvestigationViewLoadStatus.FAILED ? ERROR : ACCENT, false);
        graphics.drawString(font, Component.translatable("gui.intelligencearchive.investigation.scroll_hint"),
                124, height - 16, MUTED, false);
    }

    private String animatedRequestMessage() {
        String base = Component.translatable(
                "gui.intelligencearchive.investigation.status.requesting"
        ).getString();
        int dotCount = (int) ((System.currentTimeMillis() / 350L) % 3L) + 1;
        return base + ".".repeat(dotCount);
    }

    private void drawScrollbar(GuiGraphics graphics, Layout layout, RenderMetrics metrics, double offset) {
        if (metrics.contentHeight() <= metrics.viewportHeight() || metrics.viewportHeight() <= 0) {
            return;
        }
        int trackX = layout.contentRight() - 5;
        int trackHeight = metrics.viewportHeight();
        int thumbHeight = Math.max(16, trackHeight * metrics.viewportHeight() / metrics.contentHeight());
        int maxThumbOffset = Math.max(0, trackHeight - thumbHeight);
        double maxContentOffset = Math.max(1.0D, metrics.contentHeight() - metrics.viewportHeight());
        int thumbOffset = (int) Math.round(maxThumbOffset * Math.min(1.0D, offset / maxContentOffset));
        graphics.fill(trackX, metrics.viewportTop(), trackX + 2, metrics.viewportBottom(), PANEL_EDGE);
        graphics.fill(trackX - 1, metrics.viewportTop() + thumbOffset,
                trackX + 3, metrics.viewportTop() + thumbOffset + thumbHeight, ACCENT);
    }

    private int drawWrapped(GuiGraphics graphics, Font font, String text,
                            int x, int y, int width, int color) {
        var lines = font.split(Component.literal(text), Math.max(1, width));
        for (int index = 0; index < lines.size(); index++) {
            graphics.drawString(font, lines.get(index), x, y + index * (font.lineHeight + 2), color, false);
        }
        return lines.size() * (font.lineHeight + 2);
    }

    private void drawPanel(GuiGraphics graphics, Font font, int left, int top, int right, int bottom,
                           String titleKey) {
        graphics.fill(left, top, right, bottom, PANEL_EDGE);
        graphics.fill(left + 1, top + 1, right - 1, bottom - 1, PANEL);
        graphics.fill(left + 2, top + 2, right - 2, top + 20, GRID);
        graphics.fill(left + 2, top + 20, right - 2, top + 21, PANEL_EDGE);
        graphics.drawString(
                font,
                Component.translatable(titleKey),
                left + 8,
                top + 8,
                ACCENT,
                false
        );
    }

    private record CardPlacement(InvestigationCardRenderer.CardContent card, int column, int y, int height) {
    }

    public record RenderMetrics(int contentHeight, int viewportHeight, int viewportLeft,
                                int viewportTop, int viewportRight, int viewportBottom) {
        public boolean contains(double x, double y) {
            return x >= viewportLeft && x < viewportRight && y >= viewportTop && y < viewportBottom;
        }
    }

    public record Layout(int navigationLeft, int navigationRight, int contentLeft, int contentRight,
                         int statusLeft, int statusRight, int headerBottom, int footerTop) {
    }
}
