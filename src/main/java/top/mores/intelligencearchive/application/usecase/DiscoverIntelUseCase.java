package top.mores.intelligencearchive.application.usecase;

import top.mores.intelligencearchive.application.result.DiscoverIntelResult;
import top.mores.intelligencearchive.application.result.OperationStatus;
import top.mores.intelligencearchive.common.event.DomainEventPublisher;
import top.mores.intelligencearchive.common.event.IntelDiscoveredEvent;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryRecord;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;
import top.mores.intelligencearchive.common.service.IntelService;
import top.mores.intelligencearchive.common.service.InvestigationService;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * “玩家发现世界情报”的应用入口。
 *
 * <p>UseCase 不保存数据：它先使用 IntelService 判断世界对象是否存在，再通过
 * InvestigationService 修改玩家认知。应用层只编排流程，不替代两个 Service 的基础能力。</p>
 */
public final class DiscoverIntelUseCase {
    private final IntelService intelService;
    private final InvestigationService investigationService;
    private final DomainEventPublisher eventPublisher;
    private final Clock clock;

    public DiscoverIntelUseCase(IntelService intelService, InvestigationService investigationService) {
        this(intelService, investigationService, DomainEventPublisher.noOp(), Clock.systemUTC());
    }

    public DiscoverIntelUseCase(
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

    public DiscoverIntelResult execute(UUID playerId, String intelId) {
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
        if (oldStatus != IntelDiscoveryStatus.UNKNOWN) {
            return result(
                    OperationStatus.ALREADY_DISCOVERED,
                    intelId,
                    oldStatus,
                    oldStatus,
                    "The player has already discovered this intel."
            );
        }

        IntelDiscoveryRecord discovered = investigationService.discoverIntel(playerId, intelId);
        UseCaseSupport.publishSafely(
                eventPublisher,
                new IntelDiscoveredEvent(playerId, intelId, clock.instant())
        );
        return result(
                OperationStatus.SUCCESS,
                intelId,
                oldStatus,
                discovered.status(),
                "Intel discovered."
        );
    }

    private static DiscoverIntelResult result(
            OperationStatus status,
            String intelId,
            IntelDiscoveryStatus oldStatus,
            IntelDiscoveryStatus newStatus,
            String message
    ) {
        return new DiscoverIntelResult(status, intelId, oldStatus, newStatus, message);
    }
}
