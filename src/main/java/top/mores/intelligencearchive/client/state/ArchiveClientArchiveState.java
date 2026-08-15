package top.mores.intelligencearchive.client.state;

import top.mores.intelligencearchive.common.dto.ArchiveDocumentDTO;
import top.mores.intelligencearchive.client.view.ArchiveViewModel;

import java.util.Objects;

/**
 * 客户端最近一次档案查询的轻量视图缓存。
 *
 * <p>它只用于 Screen 展示，不保存玩家解锁列表，也不是档案权限或拥有状态的业务真相；
 * 客户端重连后会清空，所有业务裁决仍由服务端完成。</p>
 */
public final class ArchiveClientArchiveState {
    private static final long REQUEST_TIMEOUT_NANOS = 10_000_000_000L;

    private String pendingDocumentId = "";
    private long requestDeadlineNanos;
    private boolean requesting;
    private ArchiveDocumentDTO currentDocument;
    private String lastError = "";
    private String pendingResolvedDocumentId = "";
    private String currentResolvedDocumentId = "";
    private long resolvedRequestDeadlineNanos;
    private ResolvedContentLoadStatus resolvedStatus = ResolvedContentLoadStatus.IDLE;
    private ArchiveViewModel currentViewModel;
    private String resolvedError = "";
    private String lastClickedIntelId = "";

    public boolean beginRequest(String documentId, long nowNanos) {
        Objects.requireNonNull(documentId, "documentId 不能为 null");
        if (requesting) {
            return false;
        }

        pendingDocumentId = documentId;
        requestDeadlineNanos = nowNanos + REQUEST_TIMEOUT_NANOS;
        requesting = true;
        currentDocument = null;
        lastError = "";
        return true;
    }

    public void acceptDocument(String documentId, ArchiveDocumentDTO document) {
        ArchiveDocumentDTO validDocument = Objects.requireNonNull(document, "document 不能为 null");
        if (!matchesPending(documentId) || !documentId.equals(validDocument.id())) {
            return;
        }

        finishRequest();
        currentDocument = validDocument;
        lastError = "";
    }

    public void acceptError(String documentId, String errorMessage) {
        Objects.requireNonNull(errorMessage, "errorMessage 不能为 null");
        if (!matchesPending(documentId)) {
            return;
        }

        finishRequest();
        currentDocument = null;
        lastError = errorMessage;
    }

    public void failToSend(String documentId) {
        acceptError(documentId, "Unable to send the document request to the server.");
    }

    public void tick(long nowNanos) {
        if (requesting && nowNanos >= requestDeadlineNanos) {
            finishRequest();
            currentDocument = null;
            lastError = "The server did not respond within 10 seconds.";
        }
        if (resolvedStatus == ResolvedContentLoadStatus.REQUESTING
                && nowNanos >= resolvedRequestDeadlineNanos) {
            finishResolvedRequest(ResolvedContentLoadStatus.FAILED);
            currentViewModel = null;
            resolvedError = "The server did not respond within 10 seconds.";
        }
    }

    public boolean beginResolvedRequest(String documentId, long nowNanos) {
        Objects.requireNonNull(documentId, "documentId 不能为 null");
        if (resolvedStatus == ResolvedContentLoadStatus.REQUESTING) {
            return false;
        }
        pendingResolvedDocumentId = documentId;
        currentResolvedDocumentId = documentId;
        resolvedRequestDeadlineNanos = nowNanos + REQUEST_TIMEOUT_NANOS;
        resolvedStatus = ResolvedContentLoadStatus.REQUESTING;
        currentViewModel = null;
        resolvedError = "";
        return true;
    }

    public void acceptResolvedContent(String documentId, ArchiveViewModel viewModel) {
        ArchiveViewModel validViewModel = Objects.requireNonNull(viewModel, "viewModel 不能为 null");
        if (!matchesResolvedPending(documentId) || !documentId.equals(validViewModel.documentId())) {
            return;
        }
        finishResolvedRequest(ResolvedContentLoadStatus.LOADED);
        currentViewModel = validViewModel;
        resolvedError = "";
    }

    public void acceptResolvedError(String documentId, String errorMessage) {
        Objects.requireNonNull(errorMessage, "errorMessage 不能为 null");
        if (!matchesResolvedPending(documentId)) {
            return;
        }
        finishResolvedRequest(ResolvedContentLoadStatus.FAILED);
        currentViewModel = null;
        resolvedError = errorMessage;
    }

    public void failResolvedSend(String documentId) {
        acceptResolvedError(documentId, "Unable to send the resolved content request to the server.");
    }

    public void recordIntelLinkClick(String intelId) {
        lastClickedIntelId = Objects.requireNonNull(intelId, "intelId 不能为 null");
    }

    public void reset() {
        pendingDocumentId = "";
        requestDeadlineNanos = 0L;
        requesting = false;
        currentDocument = null;
        lastError = "";
        pendingResolvedDocumentId = "";
        currentResolvedDocumentId = "";
        resolvedRequestDeadlineNanos = 0L;
        resolvedStatus = ResolvedContentLoadStatus.IDLE;
        currentViewModel = null;
        resolvedError = "";
        lastClickedIntelId = "";
    }

    public View view() {
        return new View(requesting, currentDocument, lastError);
    }

    public ResolvedView resolvedView() {
        return new ResolvedView(
                currentResolvedDocumentId,
                resolvedStatus,
                currentViewModel,
                resolvedError,
                lastClickedIntelId
        );
    }

    private boolean matchesPending(String documentId) {
        return requesting && pendingDocumentId.equals(documentId);
    }

    private void finishRequest() {
        pendingDocumentId = "";
        requestDeadlineNanos = 0L;
        requesting = false;
    }

    private boolean matchesResolvedPending(String documentId) {
        return resolvedStatus == ResolvedContentLoadStatus.REQUESTING
                && pendingResolvedDocumentId.equals(documentId);
    }

    private void finishResolvedRequest(ResolvedContentLoadStatus status) {
        pendingResolvedDocumentId = "";
        resolvedRequestDeadlineNanos = 0L;
        resolvedStatus = status;
    }

    public record View(boolean requesting, ArchiveDocumentDTO currentDocument, String lastError) {
    }

    public record ResolvedView(
            String documentId,
            ResolvedContentLoadStatus status,
            ArchiveViewModel viewModel,
            String errorMessage,
            String lastClickedIntelId
    ) {
    }
}
