package top.mores.intelligencearchive.server.service;

import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryRecord;
import top.mores.intelligencearchive.common.model.investigation.IntelDiscoveryStatus;
import top.mores.intelligencearchive.common.model.investigation.InvestigationLevel;
import top.mores.intelligencearchive.common.model.investigation.PlayerInvestigationState;
import top.mores.intelligencearchive.common.service.InvestigationService;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Phase 2-C 的内存调查状态服务。
 *
 * <p>内部 Map 只是一种可替换的临时存储，不向调用方暴露。所有读操作返回不可变快照，
 * 所有写操作必须经过 Service，从而为未来数据库、审计和服务端规则保留统一边界。</p>
 *
 * <p>本类不接收 Minecraft Player，只使用 UUID；因此领域状态不会依赖实体生命周期，
 * 也可以自然迁移到数据库或跨服消息中。</p>
 */
public final class SimpleInvestigationService implements InvestigationService {
    private final Map<UUID, PlayerInvestigationState> playerStates = new LinkedHashMap<>();
    private final Clock clock;

    public SimpleInvestigationService() {
        this(Clock.systemUTC());
    }

    /** 注入 Clock 使时间行为可重复测试，也避免模型直接读取全局时间。 */
    public SimpleInvestigationService(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock 不能为 null");
    }

    @Override
    public synchronized PlayerInvestigationState getPlayerState(UUID playerId) {
        UUID validPlayerId = requirePlayerId(playerId);
        return playerStates.computeIfAbsent(validPlayerId, SimpleInvestigationService::createEmptyState);
    }

    @Override
    public synchronized IntelDiscoveryRecord discoverIntel(UUID playerId, String intelId) {
        UUID validPlayerId = requirePlayerId(playerId);
        String validIntelId = requireIntelId(intelId);
        PlayerInvestigationState currentState = getPlayerState(validPlayerId);

        Optional<IntelDiscoveryRecord> existing = currentState.findDiscovery(validIntelId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Instant now = clock.instant();
        IntelDiscoveryRecord discovered = new IntelDiscoveryRecord(
                validIntelId,
                IntelDiscoveryStatus.DISCOVERED,
                now,
                now
        );
        playerStates.put(validPlayerId, withRecord(currentState, discovered));
        return discovered;
    }

    @Override
    public synchronized Optional<IntelDiscoveryRecord> updateStatus(
            UUID playerId,
            String intelId,
            IntelDiscoveryStatus status
    ) {
        UUID validPlayerId = requirePlayerId(playerId);
        String validIntelId = requireIntelId(intelId);
        IntelDiscoveryStatus targetStatus = Objects.requireNonNull(status, "status 不能为 null");
        if (targetStatus == IntelDiscoveryStatus.UNKNOWN) {
            throw new IllegalArgumentException("不能把已发现情报更新为 UNKNOWN");
        }

        PlayerInvestigationState currentState = getPlayerState(validPlayerId);
        Optional<IntelDiscoveryRecord> currentRecord = currentState.findDiscovery(validIntelId);
        if (currentRecord.isEmpty()) {
            return Optional.empty();
        }

        IntelDiscoveryRecord previous = currentRecord.get();
        if (!previous.status().canAdvanceTo(targetStatus) || previous.status() == targetStatus) {
            return Optional.of(previous);
        }

        IntelDiscoveryRecord updated = new IntelDiscoveryRecord(
                previous.intelId(),
                targetStatus,
                previous.discoveredTime(),
                clock.instant()
        );
        playerStates.put(validPlayerId, withRecord(currentState, updated));
        return Optional.of(updated);
    }

    @Override
    public synchronized boolean hasDiscovered(UUID playerId, String intelId) {
        return getPlayerState(requirePlayerId(playerId))
                .findDiscovery(requireIntelId(intelId))
                .isPresent();
    }

    private static PlayerInvestigationState withRecord(
            PlayerInvestigationState state,
            IntelDiscoveryRecord replacement
    ) {
        List<IntelDiscoveryRecord> records = new ArrayList<>(state.discoveredIntels().size() + 1);
        boolean replaced = false;
        for (IntelDiscoveryRecord record : state.discoveredIntels()) {
            if (record.intelId().equals(replacement.intelId())) {
                records.add(replacement);
                replaced = true;
            } else {
                records.add(record);
            }
        }
        if (!replaced) {
            records.add(replacement);
        }
        return new PlayerInvestigationState(state.playerId(), records, state.investigationLevel());
    }

    private static PlayerInvestigationState createEmptyState(UUID playerId) {
        return new PlayerInvestigationState(playerId, List.of(), InvestigationLevel.NOVICE);
    }

    private static UUID requirePlayerId(UUID playerId) {
        return Objects.requireNonNull(playerId, "playerId 不能为 null");
    }

    private static String requireIntelId(String intelId) {
        Objects.requireNonNull(intelId, "intelId 不能为 null");
        if (intelId.isBlank()) {
            throw new IllegalArgumentException("intelId 不能为空");
        }
        return intelId;
    }
}
