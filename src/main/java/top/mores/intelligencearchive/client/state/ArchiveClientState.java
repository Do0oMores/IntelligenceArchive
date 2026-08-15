package top.mores.intelligencearchive.client.state;

import top.mores.intelligencearchive.network.packet.ArchiveTestResponsePacket;

/**
 * Phase 1 的轻量客户端临时状态，只用于让关闭后重新打开的 Screen 仍能看到最后响应。
 *
 * <p>它不代表玩家真正拥有的情报，也不会成为未来权限或解锁状态的数据源；
 * 那些业务状态必须由服务端维护并下发。</p>
 */
public final class ArchiveClientState {
    private static final long REQUEST_TIMEOUT_NANOS = 10_000_000_000L;

    private int nextRequestId = 1;
    private int pendingRequestId;
    private long requestDeadlineNanos;
    private Status status = Status.NOT_REQUESTED;
    private String message = "";

    public int beginRequest(long nowNanos) {
        if (status == Status.REQUESTING) {
            return -1;
        }

        int requestId = nextRequestId;
        nextRequestId = nextRequestId == Integer.MAX_VALUE ? 1 : nextRequestId + 1;
        pendingRequestId = requestId;
        requestDeadlineNanos = nowNanos + REQUEST_TIMEOUT_NANOS;
        status = Status.REQUESTING;
        message = "";
        return requestId;
    }

    public void acceptResponse(ArchiveTestResponsePacket packet) {
        // 只接收当前请求对应的响应，迟到或伪造的响应不能覆盖当前 UI 状态。
        if (status != Status.REQUESTING || packet.requestId() != pendingRequestId) {
            return;
        }

        pendingRequestId = 0;
        requestDeadlineNanos = 0L;
        status = packet.serverAccepted() ? Status.CONNECTED : Status.REJECTED;
        message = packet.message();
    }

    public void failRequest(int requestId, String failureMessage) {
        if (status != Status.REQUESTING || requestId != pendingRequestId) {
            return;
        }

        pendingRequestId = 0;
        requestDeadlineNanos = 0L;
        status = Status.ERROR;
        message = failureMessage;
    }

    public void tick(long nowNanos) {
        if (status == Status.REQUESTING && nowNanos >= requestDeadlineNanos) {
            pendingRequestId = 0;
            requestDeadlineNanos = 0L;
            status = Status.NO_RESPONSE;
            message = "The server did not respond within 10 seconds.";
        }
    }

    public void reset() {
        pendingRequestId = 0;
        requestDeadlineNanos = 0L;
        status = Status.NOT_REQUESTED;
        message = "";
    }

    public View view() {
        return new View(status, message);
    }

    public enum Status {
        NOT_REQUESTED("Not requested"),
        REQUESTING("REQUESTING..."),
        CONNECTED("CONNECTED"),
        REJECTED("REJECTED"),
        NO_RESPONSE("NO RESPONSE"),
        ERROR("ERROR");

        private final String displayText;

        Status(String displayText) {
            this.displayText = displayText;
        }

        public String displayText() {
            return displayText;
        }
    }

    public record View(Status status, String message) {
        public boolean requestInFlight() {
            return status == Status.REQUESTING;
        }
    }
}
