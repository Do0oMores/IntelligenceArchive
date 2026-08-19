package top.mores.intelligencearchive.client.state;

import top.mores.intelligencearchive.client.view.ArchiveSummaryViewModel;

import java.util.List;
import java.util.Objects;

/**
 * 客户端索引展示缓存。
 *
 * <p>它只保存服务端已经裁决并下发的摘要，不是玩家拥有列表，也不能反向修改服务端状态。</p>
 */
public final class ArchiveIndexState {
    private static final long REQUEST_TIMEOUT_NANOS = 10_000_000_000L;

    private ArchiveIndexLoadStatus status = ArchiveIndexLoadStatus.IDLE;
    private long deadlineNanos;
    private List<ArchiveSummaryViewModel> archives = List.of();
    private String errorMessage = "";

    public boolean beginRequest(long nowNanos) {
        if (status == ArchiveIndexLoadStatus.REQUESTING) {
            return false;
        }
        status = ArchiveIndexLoadStatus.REQUESTING;
        deadlineNanos = nowNanos + REQUEST_TIMEOUT_NANOS;
        archives = List.of();
        errorMessage = "";
        return true;
    }

    public void accept(List<ArchiveSummaryViewModel> visibleArchives) {
        if (status != ArchiveIndexLoadStatus.REQUESTING) {
            return;
        }
        archives = List.copyOf(Objects.requireNonNull(visibleArchives, "visibleArchives 不能为 null"));
        status = ArchiveIndexLoadStatus.LOADED;
        deadlineNanos = 0L;
        errorMessage = "";
    }

    public void fail(String message) {
        if (status != ArchiveIndexLoadStatus.REQUESTING) {
            return;
        }
        status = ArchiveIndexLoadStatus.FAILED;
        deadlineNanos = 0L;
        archives = List.of();
        errorMessage = Objects.requireNonNull(message, "message 不能为 null");
    }

    public void tick(long nowNanos) {
        if (status == ArchiveIndexLoadStatus.REQUESTING && nowNanos >= deadlineNanos) {
            fail("The server did not respond within 10 seconds.");
        }
    }

    public void reset() {
        status = ArchiveIndexLoadStatus.IDLE;
        deadlineNanos = 0L;
        archives = List.of();
        errorMessage = "";
    }

    public View view() {
        return new View(status, archives, errorMessage);
    }

    public record View(
            ArchiveIndexLoadStatus status,
            List<ArchiveSummaryViewModel> archives,
            String errorMessage
    ) {
        public View {
            archives = List.copyOf(archives);
        }
    }
}
