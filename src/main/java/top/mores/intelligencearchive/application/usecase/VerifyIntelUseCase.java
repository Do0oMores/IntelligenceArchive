package top.mores.intelligencearchive.application.usecase;

import top.mores.intelligencearchive.application.result.OperationStatus;
import top.mores.intelligencearchive.application.result.VerifyIntelResult;
import top.mores.intelligencearchive.common.event.DomainEventPublisher;
import top.mores.intelligencearchive.common.event.IntelVerifiedEvent;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryRecord;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;
import top.mores.intelligencearchive.common.service.IntelService;
import top.mores.intelligencearchive.common.service.InvestigationService;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** 将已经阅读的情报提升为 VERIFIED 的应用用例。 */
public final class VerifyIntelUseCase {
    private final IntelService intelService;
    private final InvestigationService investigationService;
    private final DomainEventPublisher eventPublisher;
    private final Clock clock;

    public VerifyIntelUseCase(IntelService intelService, InvestigationService investigationService) {
        this(intelService, investigationService, DomainEventPublisher.noOp(), Clock.systemUTC());
    }

    public VerifyIntelUseCase(
            IntelService intelService,
            InvestigationService investigationService,
            DomainEventPublisher eventPublisher,
            Clock clock
    ) {
        this.intelService = Objects.requireNonNull(intelService, "intelService 不能为 null");
        this.investigationService = Objects.requireNonNull(
                investigationService,
                "investigationService 不能为 null"
        );
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher 不能为 null");
        this.clock = Objects.requireNonNull(clock, "clock 不能为 null");
    }

    public VerifyIntelResult execute(UUID playerId, String intelId) {
        String resultIntelId = UseCaseSupport.resultId(intelId);
        if (playerId == null || UseCaseSupport.invalidId(intelId)) {
            return result(
                    OperationStatus.INVALID_INPUT,
                    resultIntelId,
                    IntelDiscoveryStatus.UNKNOWN,
                    IntelDiscoveryStatus.UNKNOWN,
                    "Player ID and intel ID are required."
            );
        }

        if (!UseCaseSupport.worldIntelExists(intelService, intelId)) {
            return result(
                    OperationStatus.INTEL_NOT_FOUND,
                    intelId,
                    IntelDiscoveryStatus.UNKNOWN,
                    IntelDiscoveryStatus.UNKNOWN,
                    "The requested world intel does not exist."
            );
        }

        IntelDiscoveryStatus oldStatus = investigationService.getPlayerState(playerId).statusOf(intelId);
        if (oldStatus == IntelDiscoveryStatus.VERIFIED || oldStatus == IntelDiscoveryStatus.ARCHIVED) {
            return result(
                    OperationStatus.SUCCESS,
                    intelId,
                    oldStatus,
                    oldStatus,
                    "The intel is already verified."
            );
        }
        if (oldStatus != IntelDiscoveryStatus.READ) {
            return result(
                    OperationStatus.INVALID_STATE,
                    intelId,
                    oldStatus,
                    oldStatus,
                    "Intel must be read before it can be verified."
            );
        }

        Optional<IntelDiscoveryRecord> updated = investigationService.updateStatus(
                playerId,
                intelId,
                IntelDiscoveryStatus.VERIFIED
        );
        if (updated.isEmpty()) {
            return result(
                    OperationStatus.INVALID_STATE,
                    intelId,
                    oldStatus,
                    oldStatus,
                    "The discovery record is no longer available."
            );
        }
        UseCaseSupport.publishSafely(
                eventPublisher,
                new IntelVerifiedEvent(playerId, intelId, clock.instant())
        );
        return result(
                OperationStatus.SUCCESS,
                intelId,
                oldStatus,
                updated.get().status(),
                "Intel verified."
        );
    }

    private static VerifyIntelResult result(
            OperationStatus status,
            String intelId,
            IntelDiscoveryStatus oldStatus,
            IntelDiscoveryStatus newStatus,
            String message
    ) {
        return new VerifyIntelResult(status, intelId, oldStatus, newStatus, message);
    }
}
