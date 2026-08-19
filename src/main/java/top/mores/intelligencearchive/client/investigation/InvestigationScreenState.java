package top.mores.intelligencearchive.client.investigation;

import top.mores.intelligencearchive.client.investigation.view.InvestigationViewModel;
import top.mores.intelligencearchive.client.investigation.view.InvestigationViewModelMapper;

import java.util.Objects;
import java.util.Optional;

/**
 * Screen 的本地显示状态。
 *
 * <p>网络 DTO 只在同步边界转换一次；Screen 与 Renderer 之后只消费 ViewModel。</p>
 */
public final class InvestigationScreenState {
    private InvestigationTab selectedTab = InvestigationTab.CASE;
    private InvestigationViewLoadStatus loadStatus = InvestigationViewLoadStatus.IDLE;
    private String currentCaseId = "";
    private InvestigationViewModel viewModel;
    private String errorMessage = "";

    public void selectTab(InvestigationTab tab) {
        selectedTab = Objects.requireNonNull(tab, "tab 不能为 null");
    }

    public void beginRequest(String caseId) {
        String nextCaseId = Objects.requireNonNull(caseId, "caseId 不能为 null");
        if (!nextCaseId.equals(currentCaseId)) {
            selectedTab = InvestigationTab.CASE;
        }
        currentCaseId = nextCaseId;
        viewModel = null;
        errorMessage = "";
        loadStatus = InvestigationViewLoadStatus.REQUESTING;
    }

    /** 从客户端缓存同步；这里是 UI 层唯一读取调查 DTO 快照的位置。 */
    public void synchronize(ClientInvestigationState.View clientView) {
        Objects.requireNonNull(clientView, "clientView 不能为 null");
        if (!clientView.currentCaseId().equals(currentCaseId)) {
            selectedTab = InvestigationTab.CASE;
        }
        currentCaseId = clientView.currentCaseId();
        errorMessage = clientView.errorMessage();
        loadStatus = clientView.loadStatus();
        if (loadStatus != InvestigationViewLoadStatus.LOADED || clientView.currentView().isEmpty()) {
            viewModel = null;
            return;
        }
        try {
            viewModel = InvestigationViewModelMapper.fromDto(clientView.currentView().orElseThrow());
        } catch (RuntimeException exception) {
            viewModel = null;
            loadStatus = InvestigationViewLoadStatus.FAILED;
            errorMessage = "The investigation view could not be displayed.";
        }
    }

    /**
     * 仅供开发环境 Fixture 注入预构建的只读 ViewModel。
     *
     * <p>该方法不经过网络、Domain 或 Service，也不参与正式调查请求生命周期，
     * 因此不会影响任何生产业务逻辑。</p>
     */
    public void applyPreview(InvestigationViewModel preview) {
        InvestigationViewModel validPreview = Objects.requireNonNull(preview, "preview 不能为 null");
        selectedTab = InvestigationTab.CASE;
        currentCaseId = validPreview.caseId();
        viewModel = validPreview;
        errorMessage = "";
        loadStatus = InvestigationViewLoadStatus.LOADED;
    }

    public Snapshot snapshot() {
        return new Snapshot(
                selectedTab,
                loadStatus,
                currentCaseId,
                Optional.ofNullable(viewModel),
                errorMessage
        );
    }

    /** 渲染阶段使用的不可变快照。 */
    public record Snapshot(
            InvestigationTab selectedTab,
            InvestigationViewLoadStatus loadStatus,
            String currentCaseId,
            Optional<InvestigationViewModel> viewModel,
            String errorMessage
    ) {
        public Snapshot {
            selectedTab = Objects.requireNonNull(selectedTab, "selectedTab 不能为 null");
            loadStatus = Objects.requireNonNull(loadStatus, "loadStatus 不能为 null");
            currentCaseId = Objects.requireNonNull(currentCaseId, "currentCaseId 不能为 null");
            viewModel = Objects.requireNonNull(viewModel, "viewModel 不能为 null");
            errorMessage = Objects.requireNonNull(errorMessage, "errorMessage 不能为 null");
        }
    }
}
