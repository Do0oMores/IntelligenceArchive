package top.mores.intelligencearchive.client.investigation;

import top.mores.intelligencearchive.common.dto.investigation.PlayerInvestigationViewDTO;

import java.util.Objects;
import java.util.Optional;

/**
 * 客户端只读调查视图缓存。
 *
 * <p>该状态只保存服务端响应 DTO，不包含权限判断、修改接口或 Domain/Service 引用，
 * 因此无法授予情报、改变服务器调查状态或推断隐藏世界关系。</p>
 */
public final class ClientInvestigationState {
    private static final long REQUEST_TIMEOUT_NANOS = 10_000_000_000L;

    private String currentCaseId = "";
    private PlayerInvestigationViewDTO currentView;
    private InvestigationViewLoadStatus loadStatus = InvestigationViewLoadStatus.IDLE;
    private String errorMessage = "";
    private long deadlineNanos;

    public boolean beginRequest(String caseId, long nowNanos) {
        Objects.requireNonNull(caseId, "caseId 不能为 null");
        if (caseId.isBlank() || loadStatus == InvestigationViewLoadStatus.REQUESTING) {
            return false;
        }
        currentCaseId = caseId;
        currentView = null;
        loadStatus = InvestigationViewLoadStatus.REQUESTING;
        errorMessage = "";
        deadlineNanos = nowNanos + REQUEST_TIMEOUT_NANOS;
        return true;
    }

    public void accept(PlayerInvestigationViewDTO view) {
        Objects.requireNonNull(view, "view 不能为 null");
        if (loadStatus != InvestigationViewLoadStatus.REQUESTING
                || !currentCaseId.equals(view.caseId())) {
            return;
        }
        currentView = view;
        loadStatus = InvestigationViewLoadStatus.LOADED;
        errorMessage = "";
        deadlineNanos = 0L;
    }

    public void fail(String message) {
        if (loadStatus != InvestigationViewLoadStatus.REQUESTING) {
            return;
        }
        currentView = null;
        loadStatus = InvestigationViewLoadStatus.FAILED;
        errorMessage = Objects.requireNonNull(message, "message 不能为 null");
        deadlineNanos = 0L;
    }

    public void tick(long nowNanos) {
        if (loadStatus == InvestigationViewLoadStatus.REQUESTING && nowNanos >= deadlineNanos) {
            fail("The server did not respond within 10 seconds.");
        }
    }

    public void reset() {
        currentCaseId = "";
        currentView = null;
        loadStatus = InvestigationViewLoadStatus.IDLE;
        errorMessage = "";
        deadlineNanos = 0L;
    }

    public View view() {
        return new View(
                currentCaseId,
                Optional.ofNullable(currentView),
                loadStatus,
                errorMessage
        );
    }

    /** 对外只暴露不可变快照，未来 Screen 不能持有或修改缓存内部字段。 */
    public record View(
            String currentCaseId,
            Optional<PlayerInvestigationViewDTO> currentView,
            InvestigationViewLoadStatus loadStatus,
            String errorMessage
    ) {
        public View {
            currentCaseId = Objects.requireNonNull(currentCaseId, "currentCaseId 不能为 null");
            currentView = Objects.requireNonNull(currentView, "currentView 不能为 null");
            loadStatus = Objects.requireNonNull(loadStatus, "loadStatus 不能为 null");
            errorMessage = Objects.requireNonNull(errorMessage, "errorMessage 不能为 null");
        }
    }
}
