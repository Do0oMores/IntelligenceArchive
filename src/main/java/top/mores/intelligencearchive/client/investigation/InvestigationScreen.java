package top.mores.intelligencearchive.client.investigation;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import top.mores.intelligencearchive.client.ArchiveClient;
import top.mores.intelligencearchive.client.investigation.component.ArchiveButton;
import top.mores.intelligencearchive.client.investigation.dev.InvestigationPreviewFixture;
import top.mores.intelligencearchive.client.investigation.render.InvestigationScreenRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 玩家调查终端的基础 Shell。
 *
 * <p>Screen 只处理标签切换、请求动作与 ViewModel 展示。它不读取 Service、Repository、
 * Domain State 或网络 DTO，也不判断玩家可见性。</p>
 */
public final class InvestigationScreen extends Screen {
    private static final int TAB_BUTTON_HEIGHT = 24;
    private static final int TAB_GAP = 5;

    private final InvestigationScreenRenderer renderer = new InvestigationScreenRenderer();
    private final InvestigationScreenState screenState = new InvestigationScreenState();
    private final InvestigationScrollState scrollState = new InvestigationScrollState();
    private final String requestedCaseId;
    private boolean initializedData;
    private String displayedCaseId = "";
    private final List<ArchiveButton> tabButtons = new ArrayList<>();
    private InvestigationScreenRenderer.RenderMetrics renderMetrics =
            new InvestigationScreenRenderer.RenderMetrics(0, 0, 0, 0, 0, 0);

    public InvestigationScreen() {
        this("");
    }

    /** 未来 Case/Archive 导航可以显式传入 caseId，并走正式服务端请求链。 */
    public InvestigationScreen(String caseId) {
        super(Component.literal("Investigation Terminal"));
        this.requestedCaseId = Objects.requireNonNull(caseId, "caseId 不能为 null");
    }

    @Override
    protected void init() {
        initializeData();
        addTabButtons();
        addRetryButton();
    }

    private void initializeData() {
        if (initializedData) {
            return;
        }
        initializedData = true;
        // 开发环境且无显式 Case 时，注入预览 Fixture 以便验证 GUI；生产环境绝不走此分支。
        if (InvestigationPreviewFixture.isActive() && requestedCaseId.isBlank()) {
            screenState.applyPreview(InvestigationPreviewFixture.preview());
            return;
        }
        if (!requestedCaseId.isBlank()) {
            screenState.beginRequest(requestedCaseId);
            ArchiveClient.requestInvestigationView(requestedCaseId);
            return;
        }
        // 无显式 Case 时允许复用当前客户端缓存；绝不自行选择或猜测服务端案件。
        ClientInvestigationState.View cached = ArchiveClient.getInvestigationViewState();
        if (!cached.currentCaseId().isBlank()) {
            screenState.synchronize(cached);
        }
    }

    private void addTabButtons() {

        tabButtons.clear();

        InvestigationScreenRenderer.Layout layout =
                renderer.layout(width, height);

        int buttonWidth =
                Math.max(
                        60,
                        layout.navigationRight()
                                - layout.navigationLeft()
                                - 12
                );
        int y = layout.headerBottom() + 31;
        for (InvestigationTab tab : InvestigationTab.values()) {
            ArchiveButton button =
                    ArchiveButton.archiveBuilder(
                                    tabMessage(tab),
                                    press -> selectTab(tab)
                            )
                            .bounds(
                                    layout.navigationLeft() + 6,
                                    y,
                                    buttonWidth,
                                    TAB_BUTTON_HEIGHT
                            )
                            .build();
            button.setSelected(
                    screenState.snapshot()
                            .selectedTab()
                            == tab
            );
            tabButtons.add(button);
            addRenderableWidget(button);
            y += TAB_BUTTON_HEIGHT + TAB_GAP;
        }
    }

    private void addRetryButton() {
        if (requestedCaseId.isBlank()) {
            return;
        }
        InvestigationScreenRenderer.Layout layout = renderer.layout(width, height);
        int buttonWidth = Math.max(
                60,
                layout.navigationRight() - layout.navigationLeft() - 12
        );
        ArchiveButton retryButton =
                ArchiveButton.archiveBuilder(
                                Component.translatable(
                                        "gui.intelligencearchive.retry"
                                ),
                                button -> retryRequest()
                        )
                        .bounds(
                                layout.navigationLeft() + 6,
                                layout.footerTop() - 28,
                                buttonWidth,
                                TAB_BUTTON_HEIGHT
                        )
                        .build();
        addRenderableWidget(retryButton);
    }

    private void retryRequest() {
        screenState.beginRequest(requestedCaseId);
        ArchiveClient.requestInvestigationView(requestedCaseId);
    }

    private void refreshTabSelection() {
        InvestigationTab selected =
                screenState.snapshot()
                        .selectedTab();
        for (int index = 0;
             index < tabButtons.size();
             index++) {
            tabButtons.get(index)
                    .setSelected(
                            InvestigationTab.values()[index]
                                    == selected
                    );
            tabButtons.get(index)
                    .setMessage(
                            tabMessage(InvestigationTab.values()[index])
                    );
        }
    }

    private Component tabMessage(InvestigationTab tab) {
        boolean selected = screenState.snapshot().selectedTab() == tab;
        return Component.literal(selected ? "[■ " : "[ ")
                .append(tab.displayName())
                .append(" ]");
    }

    private void selectTab(InvestigationTab tab) {
        if (screenState.snapshot().selectedTab() != tab) {
            screenState.selectTab(tab);
            scrollState.reset();
            refreshTabSelection();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!requestedCaseId.isBlank()) {
            screenState.synchronize(ArchiveClient.getInvestigationViewState());
        }
        String currentCaseId = screenState.snapshot().currentCaseId();
        if (!currentCaseId.equals(displayedCaseId)) {
            displayedCaseId = currentCaseId;
            scrollState.reset();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderMetrics = renderer.render(
                graphics,
                font,
                width,
                height,
                screenState.snapshot(),
                scrollState.offset()
        );
        scrollState.updateMetrics(renderMetrics.contentHeight(), renderMetrics.viewportHeight());
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (renderMetrics.contains(mouseX, mouseY) && scrollState.scrollBy(-delta * 28.0D)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP) {
            return scrollState.scrollBy(-18.0D) || super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            return scrollState.scrollBy(18.0D) || super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            return scrollState.scrollBy(-Math.max(24, scrollState.viewportHeight() - 24))
                    || super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            return scrollState.scrollBy(Math.max(24, scrollState.viewportHeight() - 24))
                    || super.keyPressed(keyCode, scanCode, modifiers);
        }
        if (keyCode == GLFW.GLFW_KEY_HOME) {
            scrollState.scrollToStart();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_END) {
            scrollState.scrollToEnd();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** 提供只读快照供 UI 行为测试，不暴露可变状态。 */
    public InvestigationScreenState.Snapshot stateSnapshot() {
        return screenState.snapshot();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
