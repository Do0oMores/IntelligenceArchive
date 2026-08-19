package top.mores.intelligencearchive.application.result;

import java.util.List;
import java.util.Objects;

/** 发现 Evidence 并执行 Clue 派生后的稳定结果。 */
public record DiscoverEvidenceResult(
        OperationStatus status,
        String caseId,
        String evidenceId,
        boolean newlyDiscoveredEvidence,
        List<String> newlyDerivedClueIds,
        String message
) implements OperationResult {
    public DiscoverEvidenceResult {
        status = Objects.requireNonNull(status, "status 不能为 null");
        caseId = Objects.requireNonNull(caseId, "caseId 不能为 null");
        evidenceId = Objects.requireNonNull(evidenceId, "evidenceId 不能为 null");
        newlyDerivedClueIds = List.copyOf(Objects.requireNonNull(newlyDerivedClueIds,
                "newlyDerivedClueIds 不能为 null"));
        message = Objects.requireNonNull(message, "message 不能为 null");
    }
}
