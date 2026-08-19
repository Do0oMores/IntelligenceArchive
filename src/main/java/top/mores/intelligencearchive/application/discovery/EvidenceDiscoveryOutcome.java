package top.mores.intelligencearchive.application.discovery;

import top.mores.intelligencearchive.common.discovery.DiscoveryStatus;

import java.util.List;
import java.util.Objects;

/** Case 外围适配器返回给 DiscoveryService 的最小 Evidence 结果。 */
public record EvidenceDiscoveryOutcome(
        DiscoveryStatus status,
        boolean newlyDiscovered,
        List<String> newlyDerivedClueIds,
        boolean investigationStateChanged,
        String message
) {
    public EvidenceDiscoveryOutcome {
        status = Objects.requireNonNull(status, "status 不能为 null");
        newlyDerivedClueIds = List.copyOf(Objects.requireNonNull(
                newlyDerivedClueIds,
                "newlyDerivedClueIds 不能为 null"
        ));
        message = Objects.requireNonNull(message, "message 不能为 null");
    }

    public static EvidenceDiscoveryOutcome notFound() {
        return new EvidenceDiscoveryOutcome(
                DiscoveryStatus.NOT_FOUND,
                false,
                List.of(),
                false,
                "The requested evidence does not exist."
        );
    }
}
