package top.mores.intelligencearchive.application.usecase;

import top.mores.intelligencearchive.application.result.OperationStatus;
import top.mores.intelligencearchive.application.result.ReadArchiveResult;
import top.mores.intelligencearchive.common.event.ArchiveReadEvent;
import top.mores.intelligencearchive.common.event.DomainEventPublisher;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryRecord;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;
import top.mores.intelligencearchive.common.service.IntelService;
import top.mores.intelligencearchive.common.service.InvestigationService;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** 将已发现档案从 DISCOVERED 提升为 READ 的应用用例。 */
public final class ReadArchiveUseCase {
    private final IntelService intelService;
    private final InvestigationService investigationService;
    private final DomainEventPublisher eventPublisher;
    private final Clock clock;

    public ReadArchiveUseCase(IntelService intelService, InvestigationService investigationService) {
        this(intelService, investigationService, DomainEventPublisher.noOp(), Clock.systemUTC());
    }

    public ReadArchiveUseCase(
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

    public ReadArchiveResult execute(UUID playerId, String documentId) {
        String resultDocumentId = UseCaseSupport.resultId(documentId);
        if (playerId == null || UseCaseSupport.invalidId(documentId)) {
            return result(
                    OperationStatus.INVALID_INPUT,
                    resultDocumentId,
                    IntelDiscoveryStatus.UNKNOWN,
                    IntelDiscoveryStatus.UNKNOWN,
                    "Player ID and document ID are required."
            );
        }

        if (intelService.findDocumentById(documentId).isEmpty()) {
            return result(
                    OperationStatus.INTEL_NOT_FOUND,
                    documentId,
                    IntelDiscoveryStatus.UNKNOWN,
                    IntelDiscoveryStatus.UNKNOWN,
                    "The requested archive document does not exist."
            );
        }

        IntelDiscoveryStatus oldStatus = investigationService.getPlayerState(playerId).statusOf(documentId);
        if (oldStatus == IntelDiscoveryStatus.UNKNOWN) {
            return result(
                    OperationStatus.INVALID_STATE,
                    documentId,
                    oldStatus,
                    oldStatus,
                    "An unknown archive cannot be read before it is discovered."
            );
        }
        if (oldStatus != IntelDiscoveryStatus.DISCOVERED) {
            return result(
                    OperationStatus.SUCCESS,
                    documentId,
                    oldStatus,
                    oldStatus,
                    "The archive has already been read."
            );
        }

        Optional<IntelDiscoveryRecord> updated = investigationService.updateStatus(
                playerId,
                documentId,
                IntelDiscoveryStatus.READ
        );
        if (updated.isEmpty()) {
            return result(
                    OperationStatus.INVALID_STATE,
                    documentId,
                    oldStatus,
                    oldStatus,
                    "The discovery record is no longer available."
            );
        }
        UseCaseSupport.publishSafely(
                eventPublisher,
                new ArchiveReadEvent(playerId, documentId, clock.instant())
        );
        return result(
                OperationStatus.SUCCESS,
                documentId,
                oldStatus,
                updated.get().status(),
                "Archive read."
        );
    }

    private static ReadArchiveResult result(
            OperationStatus status,
            String documentId,
            IntelDiscoveryStatus oldStatus,
            IntelDiscoveryStatus newStatus,
            String message
    ) {
        return new ReadArchiveResult(status, documentId, oldStatus, newStatus, message);
    }
}
