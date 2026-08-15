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
import top.mores.intelligencearchive.client.state.ArchiveClientState;
import top.mores.intelligencearchive.client.state.ResolvedContentLoadStatus;
import top.mores.intelligencearchive.client.view.ArchiveViewModel;
import top.mores.intelligencearchive.client.view.ArchiveViewNode;
import top.mores.intelligencearchive.client.view.ArchiveViewNodeType;

import java.util.ArrayList;
import java.util.List;

/**
 * 档案客户端阅读界面。
 *
 * <p>Screen 只消费 ViewModel，并把节点交给 Renderer Registry；它既不接触网络 DTO，
 * 也不判断权限、条件或解锁状态。所有显示内容都已经由服务端裁决。</p>
 */
public final class ArchiveScreen extends Screen {
    private static final int TEXT_COLOR = 0xFFE6E6E6;
    private static final int MUTED_COLOR = 0xFFAAAAAA;
    private static final int STATUS_COLOR = 0xFF80C080;
    private static final int ERROR_COLOR = 0xFFFF8080;
    private static final int NODE_GAP = 8;
    private static final double SCROLL_STEP = 18.0D;

    private final ArchiveNodeRendererRegistry rendererRegistry = ArchiveNodeRendererRegistry.createDefault();
    private final List<LinkHitbox> visibleLinkHitboxes = new ArrayList<>();

    private Button serverTestButton;
    private Button documentTestButton;
    private Button resolvedContentButton;
    private double scrollOffset;
    private int contentHeight;
    private int viewportLeft;
    private int viewportTop;
    private int viewportRight;
    private int viewportBottom;
    private String displayedContentKey = "";

    public ArchiveScreen() {
        super(Component.literal("Intelligence Archive"));
    }

    @Override
    protected void init() {
        int gap = 4;
        int availableWidth = Math.max(150, width - 24);
        int buttonWidth = Math.min(135, Math.max(48, (availableWidth - gap * 2) / 3));
        int rowWidth = buttonWidth * 3 + gap * 2;
        int left = width / 2 - rowWidth / 2;
        int buttonY = height - 24;

        serverTestButton = addRenderableWidget(Button.builder(
                Component.literal("SERVER TEST"),
                button -> ArchiveClient.requestServerTest()
        ).bounds(left, buttonY, buttonWidth, 20).build());

        documentTestButton = addRenderableWidget(Button.builder(
                Component.literal("METADATA"),
                button -> ArchiveClient.requestTestDocument()
        ).bounds(left + buttonWidth + gap, buttonY, buttonWidth, 20).build());

        resolvedContentButton = addRenderableWidget(Button.builder(
                Component.literal("REQUEST CONTENT"),
                button -> ArchiveClient.requestResolvedTestContent()
        ).bounds(left + (buttonWidth + gap) * 2, buttonY, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);

        ArchiveClientState.View serverState = ArchiveClient.getViewState();
        ArchiveClientArchiveState.View metadataState = ArchiveClient.getArchiveViewState();
        ArchiveClientArchiveState.ResolvedView resolvedState = ArchiveClient.getResolvedArchiveViewState();
        serverTestButton.active = !serverState.requestInFlight();
        documentTestButton.active = !metadataState.requesting();
        resolvedContentButton.active = resolvedState.status() != ResolvedContentLoadStatus.REQUESTING;

        viewportLeft = Math.max(12, width / 2 - 210);
        viewportRight = Math.min(width - 12, width / 2 + 210);
        viewportTop = 28;
        viewportBottom = Math.max(viewportTop + 1, height - 56);

        graphics.drawCenteredString(font, "INTELLIGENCE ARCHIVE", width / 2, 10, TEXT_COLOR);
        renderResolvedContent(graphics, resolvedState);
        renderServerStatus(graphics, serverState, resolvedState);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderResolvedContent(
            GuiGraphics graphics,
            ArchiveClientArchiveState.ResolvedView resolvedState
    ) {
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
            renderWrappedMessage(
                    graphics,
                    "CASE-TEST-001\n测试档案\n这是 IntelligenceArchive 的基础 UI。\n"
                            + "Click REQUEST CONTENT to load the server-resolved archive.",
                    TEXT_COLOR
            );
            contentHeight = 0;
            return;
        }

        ArchiveNodeRenderContext context = new MinecraftArchiveNodeRenderContext(graphics, font);
        int innerLeft = viewportLeft + 4;
        int innerWidth = Math.max(1, viewportRight - viewportLeft - 8);
        int documentHeaderHeight = context.measureWrappedText(
                "DOCUMENT: " + viewModel.documentId() + " | VERSION: " + viewModel.version(),
                innerWidth
        ) + NODE_GAP;

        int totalHeight = documentHeaderHeight;
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
        if (headerY + documentHeaderHeight >= viewportTop && headerY < viewportBottom) {
            context.drawWrappedText(
                    "DOCUMENT: " + viewModel.documentId() + " | VERSION: " + viewModel.version(),
                    innerLeft,
                    headerY,
                    innerWidth,
                    MUTED_COLOR
            );
        }

        // 先测量完整高度，但只绘制与视口相交的节点，长档案不会每帧绘制全部内容。
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

    private void renderServerStatus(
            GuiGraphics graphics,
            ArchiveClientState.View serverState,
            ArchiveClientArchiveState.ResolvedView resolvedState
    ) {
        int statusY = height - 44;
        graphics.drawString(font, "Server: " + serverState.status().displayText(), 12, statusY, STATUS_COLOR, false);
        graphics.drawString(font, "Content: " + resolvedState.status().name(), 12, statusY + 11, MUTED_COLOR, false);
        if (!resolvedState.lastClickedIntelId().isBlank()) {
            String clickText = "Link: " + resolvedState.lastClickedIntelId();
            graphics.drawString(font, clickText, Math.max(12, width - 12 - font.width(clickText)), statusY, MUTED_COLOR, false);
        }
    }

    private void renderCenteredMessage(GuiGraphics graphics, String message, int color) {
        graphics.drawCenteredString(font, message, width / 2, viewportTop + 12, color);
    }

    private void renderWrappedMessage(GuiGraphics graphics, String message, int color) {
        int lineY = viewportTop + 8;
        for (String paragraph : message.split("\\n", -1)) {
            List<FormattedCharSequence> lines = font.split(
                    Component.literal(paragraph),
                    Math.max(1, viewportRight - viewportLeft - 8)
            );
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, viewportLeft + 4, lineY, color, false);
                lineY += font.lineHeight + 3;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (isInsideViewport(mouseX, mouseY) && contentHeight > viewportHeight()) {
            scrollOffset -= delta * SCROLL_STEP;
            clampScrollOffset();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInsideViewport(mouseX, mouseY)) {
            for (LinkHitbox hitbox : visibleLinkHitboxes) {
                if (hitbox.contains(mouseX, mouseY)
                        && rendererRegistry.click(hitbox.node(), ArchiveClient::handleIntelLinkClick)) {
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
