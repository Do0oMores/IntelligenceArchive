package top.mores.intelligencearchive.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import top.mores.intelligencearchive.client.ArchiveClient;
import top.mores.intelligencearchive.client.render.ArchiveNodeRenderContext;
import top.mores.intelligencearchive.client.render.ArchiveNodeRendererRegistry;
import top.mores.intelligencearchive.client.render.MinecraftArchiveNodeRenderContext;
import top.mores.intelligencearchive.client.state.ArchiveClientArchiveState;
import top.mores.intelligencearchive.client.state.ArchiveIndexLoadStatus;
import top.mores.intelligencearchive.client.state.ArchiveIndexState;
import top.mores.intelligencearchive.client.state.ArchiveTerminalPage;
import top.mores.intelligencearchive.client.state.ArchiveTerminalState;
import top.mores.intelligencearchive.client.state.IntelNavigationState;
import top.mores.intelligencearchive.client.state.IntelNavigationStatus;
import top.mores.intelligencearchive.client.state.ResolvedContentLoadStatus;
import top.mores.intelligencearchive.client.view.ArchiveSummaryViewModel;
import top.mores.intelligencearchive.client.view.ArchiveViewModel;
import top.mores.intelligencearchive.client.view.ArchiveViewNode;
import top.mores.intelligencearchive.client.view.ArchiveViewNodeType;

import java.util.ArrayList;
import java.util.List;

/**
 * Archive Terminal 的索引与详情双页界面。
 *
 * <p>Screen 只消费客户端 ViewModel 并发出用户动作；它不查询 Repository，不判断档案是否
 * 解锁，也不解析隐藏条件。详情仍完全复用既有 Renderer Pipeline。</p>
 */
public final class ArchiveScreen extends Screen {
    private static final int TEXT_COLOR = 0xFFE6E6E6;
    private static final int MUTED_COLOR = 0xFFAAAAAA;
    private static final int STATUS_COLOR = 0xFF80C080;
    private static final int ERROR_COLOR = 0xFFFF8080;
    private static final int NODE_GAP = 8;
    private static final int INDEX_ROW_HEIGHT = 44;
    private static final double SCROLL_STEP = 18.0D;

    private final ArchiveNodeRendererRegistry rendererRegistry = ArchiveNodeRendererRegistry.createDefault();
    private final ArchiveTerminalState terminalState = new ArchiveTerminalState();
    private final List<LinkHitbox> visibleLinkHitboxes = new ArrayList<>();

    private double scrollOffset;
    private int contentHeight;
    private int viewportLeft;
    private int viewportTop;
    private int viewportRight;
    private int viewportBottom;
    private int indexPage;
    private boolean requestedInitialIndex;
    private String displayedContentKey = "";
    private String widgetStateKey = "";

    public ArchiveScreen() {
        super(Component.translatable("gui.intelligencearchive.title"));
    }

    @Override
    protected void init() {
        if (!requestedInitialIndex) {
            requestedInitialIndex = true;
            ArchiveClient.requestArchiveIndex();
        }
        rebuildWidgets();
    }

    @Override
    public void tick() {
        super.tick();
        if (!createWidgetStateKey().equals(widgetStateKey)) {
            rebuildWidgets();
        }
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();
        if (terminalState.page() == ArchiveTerminalPage.INDEX) {
            addIndexWidgets();
        } else {
            addRenderableWidget(Button.builder(Component.literal("< INDEX"), button -> showIndex())
                    .bounds(12, 8, 72, 20)
                    .build());
        }
        widgetStateKey = createWidgetStateKey();
    }

    private void addIndexWidgets() {
        ArchiveIndexState.View indexState = ArchiveClient.getArchiveIndexViewState();
        if (indexState.status() == ArchiveIndexLoadStatus.FAILED) {
            addRenderableWidget(Button.builder(Component.literal("RETRY"), button -> ArchiveClient.requestArchiveIndex())
                    .bounds(width / 2 - 50, Math.max(54, height / 2 + 18), 100, 20)
                    .build());
            return;
        }
        if (indexState.status() != ArchiveIndexLoadStatus.LOADED) {
            return;
        }

        int pageSize = indexPageSize();
        int pageCount = Math.max(1, (indexState.archives().size() + pageSize - 1) / pageSize);
        indexPage = Math.max(0, Math.min(indexPage, pageCount - 1));
        int start = indexPage * pageSize;
        int end = Math.min(indexState.archives().size(), start + pageSize);
        int left = Math.max(16, width / 2 - 210);
        for (int index = start; index < end; index++) {
            ArchiveSummaryViewModel archive = indexState.archives().get(index);
            int rowY = 42 + (index - start) * INDEX_ROW_HEIGHT;
            addRenderableWidget(Button.builder(
                            Component.literal(archive.title()),
                            button -> openDocument(archive.documentId())
                    )
                    .bounds(left, rowY, 174, 20)
                    .build());
        }

        int navigationY = Math.max(42, height - 28);
        if (indexPage > 0) {
            addRenderableWidget(Button.builder(Component.literal("<"), button -> {
                        indexPage--;
                        rebuildWidgets();
                    })
                    .bounds(width / 2 - 54, navigationY, 24, 20)
                    .build());
        }
        if (indexPage + 1 < pageCount) {
            addRenderableWidget(Button.builder(Component.literal(">"), button -> {
                        indexPage++;
                        rebuildWidgets();
                    })
                    .bounds(width / 2 + 30, navigationY, 24, 20)
                    .build());
        }
    }

    private void openDocument(String documentId) {
        terminalState.openDocument(documentId);
        scrollOffset = 0.0D;
        ArchiveClient.requestResolvedContent(documentId);
        rebuildWidgets();
    }

    private void showIndex() {
        terminalState.showIndex();
        indexPage = 0;
        // 详情成功读取后服务端可能已将状态推进为 READ，返回索引时刷新摘要。
        ArchiveClient.requestArchiveIndex();
        rebuildWidgets();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "gui.intelligencearchive.title"
                ),
                width / 2,
                12,
                TEXT_COLOR
        );
        if (terminalState.page() == ArchiveTerminalPage.INDEX) {
            renderIndex(graphics);
        } else {
            renderDetail(graphics);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderIndex(GuiGraphics graphics) {
        ArchiveIndexState.View indexState = ArchiveClient.getArchiveIndexViewState();
        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "gui.intelligencearchive.index.title"
                ),
                width / 2,
                27,
                MUTED_COLOR
        );
        if (indexState.status() == ArchiveIndexLoadStatus.REQUESTING
                || indexState.status() == ArchiveIndexLoadStatus.IDLE) {
            graphics.drawCenteredString(font, Component.translatable(
                    "gui.intelligencearchive.index.requesting"
            ), width / 2, 55, MUTED_COLOR);
            return;
        }
        if (indexState.status() == ArchiveIndexLoadStatus.FAILED) {
            renderCenteredWrappedMessage(graphics, "ERROR: " + indexState.errorMessage(), 52, ERROR_COLOR);
            return;
        }
        if (indexState.archives().isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable(
                    "gui.intelligencearchive.index.empty"
            ), width / 2, 55, MUTED_COLOR);
            return;
        }

        int pageSize = indexPageSize();
        int start = indexPage * pageSize;
        int end = Math.min(indexState.archives().size(), start + pageSize);
        int left = Math.max(16, width / 2 - 210);
        for (int index = start; index < end; index++) {
            ArchiveSummaryViewModel archive = indexState.archives().get(index);
            int rowY = 42 + (index - start) * INDEX_ROW_HEIGHT;
            graphics.drawString(
                    font,
                    "TYPE: " + archive.type() + "  STATUS: " + archive.status().name(),
                    left + 184,
                    rowY + 2,
                    STATUS_COLOR,
                    false
            );
            String summary = font.plainSubstrByWidth(archive.summary(), 400);
            graphics.drawString(font, summary, left, rowY + 25, MUTED_COLOR, false);
        }
        graphics.drawCenteredString(
                font,
                "VISIBLE: " + indexState.archives().size() + " / LIMIT 50",
                width / 2,
                Math.max(42, height - 16),
                MUTED_COLOR
        );
    }

    private int indexPageSize() {
        return Math.max(1, Math.min(6, (Math.max(100, height) - 88) / INDEX_ROW_HEIGHT));
    }

    private void renderDetail(GuiGraphics graphics) {
        ArchiveClientArchiveState.ResolvedView resolvedState = ArchiveClient.getResolvedArchiveViewState();
        viewportLeft = Math.max(12, width / 2 - 210);
        viewportRight = Math.min(width - 12, width / 2 + 210);
        viewportTop = 36;
        viewportBottom = Math.max(viewportTop + 1, height - 30);
        renderResolvedContent(graphics, resolvedState);
        renderContentStatus(graphics, resolvedState, ArchiveClient.getIntelNavigationViewState());
    }

    private void renderResolvedContent(GuiGraphics graphics, ArchiveClientArchiveState.ResolvedView resolvedState) {
        visibleLinkHitboxes.clear();
        ArchiveViewModel viewModel = resolvedState.viewModel();
        String contentKey = viewModel == null ? "" : viewModel.contentId() + ":" + viewModel.version();
        if (!displayedContentKey.equals(contentKey)) {
            displayedContentKey = contentKey;
            scrollOffset = 0.0D;
        }

        if (resolvedState.status() == ResolvedContentLoadStatus.REQUESTING) {
            renderCenteredMessage(graphics, "REQUESTING RESOLVED CONTENT...", MUTED_COLOR);
            contentHeight = 0;
            return;
        }
        if (resolvedState.status() == ResolvedContentLoadStatus.FAILED) {
            renderWrappedMessage(graphics, "ERROR: " + resolvedState.errorMessage(), ERROR_COLOR);
            contentHeight = 0;
            return;
        }
        if (resolvedState.status() != ResolvedContentLoadStatus.LOADED || viewModel == null) {
            renderWrappedMessage(graphics, "No archive content is available.", TEXT_COLOR);
            contentHeight = 0;
            return;
        }

        ArchiveNodeRenderContext context = new MinecraftArchiveNodeRenderContext(graphics, font);
        int innerLeft = viewportLeft + 4;
        int innerWidth = Math.max(1, viewportRight - viewportLeft - 8);
        int headerHeight = context.measureWrappedText(
                "DOCUMENT: " + viewModel.documentId() + " | VERSION: " + viewModel.version(),
                innerWidth
        ) + NODE_GAP;
        int totalHeight = headerHeight;
        List<MeasuredNode> measuredNodes = new ArrayList<>(viewModel.nodes().size());
        for (ArchiveViewNode node : viewModel.nodes()) {
            int nodeHeight = Math.max(context.lineHeight(), rendererRegistry.measure(context, node, innerWidth));
            measuredNodes.add(new MeasuredNode(node, totalHeight, nodeHeight));
            totalHeight += nodeHeight + NODE_GAP;
        }
        contentHeight = Math.max(0, totalHeight - NODE_GAP);
        clampScrollOffset();

        graphics.enableScissor(viewportLeft, viewportTop, viewportRight, viewportBottom);
        int headerY = viewportTop - (int) scrollOffset;
        if (headerY + headerHeight >= viewportTop && headerY < viewportBottom) {
            context.drawWrappedText(
                    "DOCUMENT: " + viewModel.documentId() + " | VERSION: " + viewModel.version(),
                    innerLeft,
                    headerY,
                    innerWidth,
                    MUTED_COLOR
            );
        }
        for (MeasuredNode measuredNode : measuredNodes) {
            int nodeY = viewportTop + measuredNode.offsetY() - (int) scrollOffset;
            if (nodeY + measuredNode.height() < viewportTop || nodeY >= viewportBottom) {
                continue;
            }
            rendererRegistry.render(context, measuredNode.node(), innerLeft, nodeY, innerWidth);
            if (measuredNode.node().type() == ArchiveViewNodeType.INTEL_LINK) {
                visibleLinkHitboxes.add(new LinkHitbox(
                        innerLeft,
                        nodeY,
                        innerWidth,
                        measuredNode.height(),
                        measuredNode.node()
                ));
            }
        }
        graphics.disableScissor();
    }

    private void renderContentStatus(
            GuiGraphics graphics,
            ArchiveClientArchiveState.ResolvedView contentState,
            IntelNavigationState.View navigationState
    ) {
        int statusY = height - 18;
        graphics.drawString(font, "Content: " + contentState.status().name(), 12, statusY, STATUS_COLOR, false);
        String navigationText = switch (navigationState.status()) {
            case REQUESTING -> "Link: REQUESTING...";
            case UNKNOWN -> "Link: UNKNOWN TARGET";
            case FAILED -> "Link: FAILED";
            case RESOLVED -> navigationState.title().isBlank()
                    ? "Link: " + navigationState.targetId()
                    : "Link: " + navigationState.title();
            case IDLE -> "";
        };
        if (!navigationText.isBlank()) {
            int color = navigationState.status() == IntelNavigationStatus.FAILED ? ERROR_COLOR : MUTED_COLOR;
            graphics.drawString(font, navigationText, Math.max(90, width - 12 - font.width(navigationText)), statusY, color, false);
        }
    }

    private void renderCenteredMessage(GuiGraphics graphics, String message, int color) {
        graphics.drawCenteredString(font, message, width / 2, viewportTop + 12, color);
    }

    private void renderCenteredWrappedMessage(GuiGraphics graphics, String message, int y, int color) {
        List<FormattedCharSequence> lines = font.split(Component.literal(message), Math.max(100, width - 40));
        for (int index = 0; index < lines.size(); index++) {
            FormattedCharSequence line = lines.get(index);
            graphics.drawString(font, line, (width - font.width(line)) / 2, y + index * (font.lineHeight + 2), color, false);
        }
    }

    private void renderWrappedMessage(GuiGraphics graphics, String message, int color) {
        int lineY = viewportTop + 8;
        for (String paragraph : message.split("\\n", -1)) {
            for (FormattedCharSequence line : font.split(Component.literal(paragraph), Math.max(1, viewportRight - viewportLeft - 8))) {
                graphics.drawString(font, line, viewportLeft + 4, lineY, color, false);
                lineY += font.lineHeight + 3;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (terminalState.page() == ArchiveTerminalPage.DETAIL
                && isInsideViewport(mouseX, mouseY)
                && contentHeight > viewportHeight()) {
            scrollOffset -= delta * SCROLL_STEP;
            clampScrollOffset();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (terminalState.page() == ArchiveTerminalPage.DETAIL && button == 0 && isInsideViewport(mouseX, mouseY)) {
            for (LinkHitbox hitbox : visibleLinkHitboxes) {
                if (hitbox.contains(mouseX, mouseY)
                        && rendererRegistry.click(hitbox.node(), ArchiveClient::handleIntelLinkClick)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private String createWidgetStateKey() {
        ArchiveIndexState.View index = ArchiveClient.getArchiveIndexViewState();
        return terminalState.page() + ":" + index.status() + ":" + index.archives().hashCode() + ":" + indexPage;
    }

    private boolean isInsideViewport(double x, double y) {
        return x >= viewportLeft && x < viewportRight && y >= viewportTop && y < viewportBottom;
    }

    private int viewportHeight() {
        return Math.max(0, viewportBottom - viewportTop);
    }

    private void clampScrollOffset() {
        scrollOffset = Math.max(0.0D, Math.min(scrollOffset, Math.max(0, contentHeight - viewportHeight())));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record MeasuredNode(ArchiveViewNode node, int offsetY, int height) {
    }

    private record LinkHitbox(int x, int y, int width, int height, ArchiveViewNode node) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}
