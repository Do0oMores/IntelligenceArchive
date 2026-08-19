package top.mores.intelligencearchive.application.discovery;

import top.mores.intelligencearchive.application.result.DiscoverIntelResult;
import top.mores.intelligencearchive.application.result.OperationStatus;
import top.mores.intelligencearchive.application.usecase.DiscoverIntelUseCase;
import top.mores.intelligencearchive.common.discovery.DiscoveryResult;
import top.mores.intelligencearchive.common.discovery.DiscoveryService;
import top.mores.intelligencearchive.common.discovery.DiscoveryStatus;
import top.mores.intelligencearchive.common.discovery.DiscoveryTargetType;
import top.mores.intelligencearchive.common.discovery.IntelDiscoverySource;
import top.mores.intelligencearchive.common.event.DomainEventPublisher;
import top.mores.intelligencearchive.common.event.EvidenceDiscoveredEvent;
import top.mores.intelligencearchive.common.event.IntelDiscoveryRequestedEvent;
import top.mores.intelligencearchive.common.service.IntelService;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 外部系统到既有发现 UseCase 的统一应用入口。
 *
 * <p>它不监听任何游戏事件，不判断发现条件，也不包含 Case 规则。Intel/Archive 委托既有
 * DiscoverIntelUseCase；Evidence 委托可替换外围网关。</p>
 */
public final class DefaultDiscoveryService implements DiscoveryService {
    private final IntelService intelService;
    private final DiscoverIntelUseCase discoverIntelUseCase;
    private final EvidenceDiscoveryGateway evidenceGateway;
    private final DomainEventPublisher eventPublisher;
    private final Clock clock;

    public DefaultDiscoveryService(
            IntelService intelService,
            DiscoverIntelUseCase discoverIntelUseCase,
            EvidenceDiscoveryGateway evidenceGateway,
            DomainEventPublisher eventPublisher,
            Clock clock
    ) {
        this.intelService = Objects.requireNonNull(intelService, "intelService 不能为 null");
        this.discoverIntelUseCase = Objects.requireNonNull(
                discoverIntelUseCase,
                "discoverIntelUseCase 不能为 null"
        );
        this.evidenceGateway = Objects.requireNonNull(evidenceGateway, "evidenceGateway 不能为 null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher 不能为 null");
        this.clock = Objects.requireNonNull(clock, "clock 不能为 null");
    }

    @Override
    public DiscoveryResult discoverIntel(UUID playerId, String intelId, IntelDiscoverySource source) {
        IntelDiscoverySource validSource = Objects.requireNonNull(source, "source 不能为 null");
        String resultId = intelId == null ? "" : intelId;
        if (playerId == null || invalidId(intelId)) {
            return unchanged(
                    DiscoveryStatus.INVALID_TARGET,
                    DiscoveryTargetType.UNKNOWN,
                    resultId,
                    validSource,
                    "Player ID and intel ID are required."
            );
        }

        publishSafely(new IntelDiscoveryRequestedEvent(playerId, intelId, validSource, clock.instant()));
        boolean archiveExists = intelService.findDocumentById(intelId).isPresent();
        boolean nodeExists = intelService.findNodeById(intelId).isPresent();
        if (!archiveExists && !nodeExists) {
            return unchanged(
                    DiscoveryStatus.NOT_FOUND,
                    DiscoveryTargetType.UNKNOWN,
                    intelId,
                    validSource,
                    "The requested world intel does not exist."
            );
        }
        if (archiveExists && nodeExists) {
            return unchanged(
                    DiscoveryStatus.INVALID_TARGET,
                    DiscoveryTargetType.UNKNOWN,
                    intelId,
                    validSource,
                    "The target ID resolves to more than one intel type."
            );
        }

        DiscoveryTargetType targetType = archiveExists
                ? DiscoveryTargetType.ARCHIVE
                : DiscoveryTargetType.INTEL;
        DiscoverIntelResult result = discoverIntelUseCase.execute(playerId, intelId);
        DiscoveryStatus status = mapStatus(result.status());
        boolean changed = status == DiscoveryStatus.SUCCESS;
        return new DiscoveryResult(
                status,
                targetType,
                intelId,
                validSource,
                changed,
                List.of(),
                changed,
                result.message()
        );
    }

    @Override
    public DiscoveryResult discoverEvidence(UUID playerId, String evidenceId, IntelDiscoverySource source) {
        IntelDiscoverySource validSource = Objects.requireNonNull(source, "source 不能为 null");
        String resultId = evidenceId == null ? "" : evidenceId;
        if (playerId == null || invalidId(evidenceId)) {
            return unchanged(
                    DiscoveryStatus.INVALID_TARGET,
                    DiscoveryTargetType.EVIDENCE,
                    resultId,
                    validSource,
                    "Player ID and evidence ID are required."
            );
        }

        EvidenceDiscoveryOutcome outcome = Objects.requireNonNull(
                evidenceGateway.discover(playerId, evidenceId),
                "EvidenceDiscoveryGateway 不能返回 null"
        );
        DiscoveryResult result = new DiscoveryResult(
                outcome.status(),
                DiscoveryTargetType.EVIDENCE,
                evidenceId,
                validSource,
                outcome.newlyDiscovered(),
                outcome.newlyDerivedClueIds(),
                outcome.investigationStateChanged(),
                outcome.message()
        );
        if (result.status() == DiscoveryStatus.SUCCESS) {
            publishSafely(new EvidenceDiscoveredEvent(
                    playerId,
                    evidenceId,
                    validSource,
                    result.newlyDerivedClueIds(),
                    clock.instant()
            ));
        }
        return result;
    }

    private static DiscoveryStatus mapStatus(OperationStatus status) {
        return switch (status) {
            case SUCCESS -> DiscoveryStatus.SUCCESS;
            case ALREADY_DISCOVERED -> DiscoveryStatus.ALREADY_DISCOVERED;
            case INTEL_NOT_FOUND -> DiscoveryStatus.NOT_FOUND;
            default -> DiscoveryStatus.INVALID_TARGET;
        };
    }

    private static DiscoveryResult unchanged(
            DiscoveryStatus status,
            DiscoveryTargetType targetType,
            String targetId,
            IntelDiscoverySource source,
            String message
    ) {
        return new DiscoveryResult(status, targetType, targetId, source, false, List.of(), false, message);
    }

    private void publishSafely(top.mores.intelligencearchive.common.event.ArchiveEvent event) {
        try {
            eventPublisher.publish(event);
        } catch (RuntimeException ignored) {
            // 外围监听器失败不能撤销或阻止玩家情报发现。
        }
    }

    private static boolean invalidId(String value) {
        return value == null || value.isBlank();
    }
}
