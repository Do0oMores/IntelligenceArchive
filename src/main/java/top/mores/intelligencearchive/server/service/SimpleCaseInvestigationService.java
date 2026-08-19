package top.mores.intelligencearchive.server.service;

import top.mores.intelligencearchive.common.casefile.service.CaseInvestigationService;
import top.mores.intelligencearchive.common.casefile.state.HypothesisStatus;
import top.mores.intelligencearchive.common.casefile.state.PlayerCaseInvestigationState;
import top.mores.intelligencearchive.common.casefile.state.PlayerInvestigationEdge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Phase 4-B 的玩家案件状态内存实现。
 *
 * <p>每个 (playerId, caseId) 使用独立快照。这里没有 IntelRepository 引用，因此玩家关系
 * 不存在写入世界 IntelEdge 的路径。</p>
 */
public final class SimpleCaseInvestigationService implements CaseInvestigationService {
    private final Map<StateKey, PlayerCaseInvestigationState> states = new LinkedHashMap<>();
    public SimpleCaseInvestigationService() {
    }

    @Override
    public synchronized PlayerCaseInvestigationState getState(UUID playerId, String caseId) {
        StateKey key = key(playerId, caseId);
        return states.computeIfAbsent(key, ignored -> emptyState(key));
    }

    @Override
    public synchronized boolean discoverEvidence(UUID playerId, String caseId, String evidenceId) {
        String validEvidenceId = requireId(evidenceId, "evidenceId");
        StateKey key = key(playerId, caseId);
        PlayerCaseInvestigationState current = getState(key.playerId(), key.caseId());
        if (current.discoveredEvidenceIds().contains(validEvidenceId)) {
            return false;
        }
        LinkedHashSet<String> evidenceIds = new LinkedHashSet<>(current.discoveredEvidenceIds());
        evidenceIds.add(validEvidenceId);
        states.put(key, copy(current, evidenceIds, current.discoveredClueIds(),
                current.investigationEdges(), current.hypothesisStatuses()));
        return true;
    }

    @Override
    public synchronized boolean discoverClue(UUID playerId, String caseId, String clueId) {
        String validClueId = requireId(clueId, "clueId");
        StateKey key = key(playerId, caseId);
        PlayerCaseInvestigationState current = getState(key.playerId(), key.caseId());
        if (current.discoveredClueIds().contains(validClueId)) {
            return false;
        }
        LinkedHashSet<String> clueIds = new LinkedHashSet<>(current.discoveredClueIds());
        clueIds.add(validClueId);
        states.put(key, copy(current, current.discoveredEvidenceIds(), clueIds,
                current.investigationEdges(), current.hypothesisStatuses()));
        return true;
    }

    @Override
    public synchronized boolean addInvestigationEdge(
            UUID playerId,
            String caseId,
            PlayerInvestigationEdge edge
    ) {
        PlayerInvestigationEdge validEdge = Objects.requireNonNull(edge, "edge 不能为 null");
        StateKey key = key(playerId, caseId);
        PlayerCaseInvestigationState current = getState(key.playerId(), key.caseId());
        if (current.containsEquivalentEdge(validEdge)) {
            return false;
        }
        List<PlayerInvestigationEdge> edges = new ArrayList<>(current.investigationEdges());
        edges.add(validEdge);
        states.put(key, copy(current, current.discoveredEvidenceIds(), current.discoveredClueIds(),
                edges, current.hypothesisStatuses()));
        return true;
    }

    @Override
    public synchronized void updateHypothesisStatus(
            UUID playerId,
            String caseId,
            String hypothesisId,
            HypothesisStatus status
    ) {
        String validHypothesisId = requireId(hypothesisId, "hypothesisId");
        HypothesisStatus validStatus = Objects.requireNonNull(status, "status 不能为 null");
        StateKey key = key(playerId, caseId);
        PlayerCaseInvestigationState current = getState(key.playerId(), key.caseId());
        LinkedHashMap<String, HypothesisStatus> statuses = new LinkedHashMap<>(current.hypothesisStatuses());
        statuses.put(validHypothesisId, validStatus);
        states.put(key, copy(current, current.discoveredEvidenceIds(), current.discoveredClueIds(),
                current.investigationEdges(), statuses));
    }

    private static PlayerCaseInvestigationState copy(
            PlayerCaseInvestigationState current,
            Set<String> evidenceIds,
            Set<String> clueIds,
            List<PlayerInvestigationEdge> edges,
            Map<String, HypothesisStatus> statuses
    ) {
        return new PlayerCaseInvestigationState(
                current.playerId(),
                current.caseId(),
                evidenceIds,
                clueIds,
                edges,
                statuses
        );
    }

    private static PlayerCaseInvestigationState emptyState(StateKey key) {
        return new PlayerCaseInvestigationState(
                key.playerId(),
                key.caseId(),
                Set.of(),
                Set.of(),
                List.of(),
                Map.of()
        );
    }

    private static StateKey key(UUID playerId, String caseId) {
        return new StateKey(
                Objects.requireNonNull(playerId, "playerId 不能为 null"),
                requireId(caseId, "caseId")
        );
    }

    private static String requireId(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " 不能为 null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        return value;
    }

    private record StateKey(UUID playerId, String caseId) {
    }
}
