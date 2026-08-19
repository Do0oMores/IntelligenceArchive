package top.mores.intelligencearchive.server.integration.casefile;

import top.mores.intelligencearchive.application.discovery.EvidenceDiscoveryGateway;
import top.mores.intelligencearchive.application.discovery.EvidenceDiscoveryOutcome;
import top.mores.intelligencearchive.application.result.DiscoverEvidenceResult;
import top.mores.intelligencearchive.application.result.OperationStatus;
import top.mores.intelligencearchive.application.usecase.DiscoverEvidenceUseCase;
import top.mores.intelligencearchive.common.discovery.DiscoveryStatus;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 将通用 Evidence Discovery 调用适配到现有 Case Evidence 用例。
 *
 * <p>Case ID 的解析被限制在该外围适配器；DefaultDiscoveryService 不导入任何 Case 类型，
 * 也不知道 Evidence 会派生哪些 Clue。</p>
 */
public final class CaseEvidenceDiscoveryGateway implements EvidenceDiscoveryGateway {
    private final CaseEvidenceLocator evidenceLocator;
    private final DiscoverEvidenceUseCase discoverEvidenceUseCase;

    public CaseEvidenceDiscoveryGateway(
            CaseEvidenceLocator evidenceLocator,
            DiscoverEvidenceUseCase discoverEvidenceUseCase
    ) {
        this.evidenceLocator = Objects.requireNonNull(evidenceLocator, "evidenceLocator 不能为 null");
        this.discoverEvidenceUseCase = Objects.requireNonNull(
                discoverEvidenceUseCase,
                "discoverEvidenceUseCase 不能为 null"
        );
    }

    @Override
    public EvidenceDiscoveryOutcome discover(UUID playerId, String evidenceId) {
        String caseId = evidenceLocator.findCaseId(evidenceId).orElse(null);
        if (caseId == null) {
            return EvidenceDiscoveryOutcome.notFound();
        }

        DiscoverEvidenceResult result = discoverEvidenceUseCase.execute(playerId, caseId, evidenceId);
        DiscoveryStatus status = mapStatus(result.status());
        if (status != DiscoveryStatus.SUCCESS) {
            return new EvidenceDiscoveryOutcome(status, false, List.of(), false, result.message());
        }
        return new EvidenceDiscoveryOutcome(
                DiscoveryStatus.SUCCESS,
                result.newlyDiscoveredEvidence(),
                result.newlyDerivedClueIds(),
                true,
                result.message()
        );
    }

    private static DiscoveryStatus mapStatus(OperationStatus status) {
        return switch (status) {
            case SUCCESS -> DiscoveryStatus.SUCCESS;
            case ALREADY_DISCOVERED -> DiscoveryStatus.ALREADY_DISCOVERED;
            case CASE_NOT_FOUND, EVIDENCE_NOT_FOUND -> DiscoveryStatus.NOT_FOUND;
            default -> DiscoveryStatus.INVALID_TARGET;
        };
    }
}
