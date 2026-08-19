package top.mores.intelligencearchive.client.state;

import top.mores.intelligencearchive.common.model.IntelNavigationTargetType;
import top.mores.intelligencearchive.network.packet.ResponseIntelNavigationPacket;

import java.util.Objects;

/** IntelLink 的轻量临时响应状态；不会保存或授予情报权限。 */
public final class IntelNavigationState {
    private static final long REQUEST_TIMEOUT_NANOS = 10_000_000_000L;

    private IntelNavigationStatus status = IntelNavigationStatus.IDLE;
    private String targetId = "";
    private IntelNavigationTargetType targetType = IntelNavigationTargetType.UNKNOWN;
    private String title = "";
    private String description = "";
    private String documentId = "";
    private String message = "";
    private long deadlineNanos;

    public boolean beginRequest(String requestedTargetId, long nowNanos) {
        Objects.requireNonNull(requestedTargetId, "requestedTargetId 不能为 null");
        if (status == IntelNavigationStatus.REQUESTING) {
            return false;
        }
        status = IntelNavigationStatus.REQUESTING;
        targetId = requestedTargetId;
        targetType = IntelNavigationTargetType.UNKNOWN;
        title = "";
        description = "";
        documentId = "";
        message = "";
        deadlineNanos = nowNanos + REQUEST_TIMEOUT_NANOS;
        return true;
    }

    public void accept(ResponseIntelNavigationPacket response) {
        Objects.requireNonNull(response, "response 不能为 null");
        if (status != IntelNavigationStatus.REQUESTING || !targetId.equals(response.targetId())) {
            return;
        }
        deadlineNanos = 0L;
        targetType = response.targetType();
        title = response.title();
        description = response.description();
        documentId = response.documentId();
        message = response.message();
        if (!response.success()) {
            status = IntelNavigationStatus.FAILED;
        } else if (response.targetType() == IntelNavigationTargetType.UNKNOWN) {
            status = IntelNavigationStatus.UNKNOWN;
        } else {
            status = IntelNavigationStatus.RESOLVED;
        }
    }

    public void fail(String failedTargetId, String errorMessage) {
        if (status != IntelNavigationStatus.REQUESTING || !targetId.equals(failedTargetId)) {
            return;
        }
        status = IntelNavigationStatus.FAILED;
        deadlineNanos = 0L;
        message = Objects.requireNonNull(errorMessage, "errorMessage 不能为 null");
    }

    public void tick(long nowNanos) {
        if (status == IntelNavigationStatus.REQUESTING && nowNanos >= deadlineNanos) {
            fail(targetId, "The server did not respond within 10 seconds.");
        }
    }

    public void reset() {
        status = IntelNavigationStatus.IDLE;
        targetId = "";
        targetType = IntelNavigationTargetType.UNKNOWN;
        title = "";
        description = "";
        documentId = "";
        message = "";
        deadlineNanos = 0L;
    }

    public View view() {
        return new View(status, targetId, targetType, title, description, documentId, message);
    }

    public record View(
            IntelNavigationStatus status,
            String targetId,
            IntelNavigationTargetType targetType,
            String title,
            String description,
            String documentId,
            String message
    ) {
    }
}
