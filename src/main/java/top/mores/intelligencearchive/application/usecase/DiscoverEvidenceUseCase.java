package top.mores.intelligencearchive.application.usecase;

import top.mores.intelligencearchive.application.result.DiscoverEvidenceResult;
import top.mores.intelligencearchive.application.result.OperationStatus;
import top.mores.intelligencearchive.common.casefile.model.CaseDefinition;
import top.mores.intelligencearchive.common.casefile.model.ClueDefinition;
import top.mores.intelligencearchive.common.casefile.model.InvestigationRequirements;
import top.mores.intelligencearchive.common.casefile.service.CaseDefinitionService;
import top.mores.intelligencearchive.common.casefile.service.CaseInvestigationService;
import top.mores.intelligencearchive.common.casefile.state.PlayerCaseInvestigationState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 发现 Evidence 并派生当前可获得的 Clue。
 *
 * <p>派生采用固定点计算，因此一次发现可连续解锁依赖于新 Clue 的后续 Clue；它不会
 * 创建任何玩家图谱关系，也不会替玩家推断世界真相。</p>
 */
public final class DiscoverEvidenceUseCase {
    private final CaseDefinitionService definitionService;
    private final CaseInvestigationService investigationService;

    public DiscoverEvidenceUseCase(
            CaseDefinitionService definitionService,
            CaseInvestigationService investigationService
    ) {
        this.definitionService = Objects.requireNonNull(definitionService, "definitionService 不能为 null");
        this.investigationService = Objects.requireNonNull(investigationService, "investigationService 不能为 null");
    }

    public DiscoverEvidenceResult execute(UUID playerId, String caseId, String evidenceId) {
        String resultCaseId = UseCaseSupport.resultId(caseId);
        String resultEvidenceId = UseCaseSupport.resultId(evidenceId);
        if (playerId == null || UseCaseSupport.invalidId(caseId) || UseCaseSupport.invalidId(evidenceId)) {
            return result(OperationStatus.INVALID_INPUT, resultCaseId, resultEvidenceId, false, List.of(),
                    "Player、Case 与 Evidence ID 必须有效。");
        }

        CaseDefinition definition = definitionService.findCase(caseId).orElse(null);
        if (definition == null) {
            return result(OperationStatus.CASE_NOT_FOUND, caseId, evidenceId, false, List.of(),
                    "Case 不存在。");
        }
        if (definition.findEvidence(evidenceId).isEmpty()) {
            return result(OperationStatus.EVIDENCE_NOT_FOUND, caseId, evidenceId, false, List.of(),
                    "Evidence 不属于该 Case。");
        }

        boolean discovered = investigationService.discoverEvidence(playerId, caseId, evidenceId);
        if (!discovered) {
            return result(OperationStatus.ALREADY_DISCOVERED, caseId, evidenceId, false, List.of(),
                    "Evidence 已经发现，状态保持不变。");
        }

        List<String> newlyDerived = deriveClues(playerId, definition);
        return result(OperationStatus.SUCCESS, caseId, evidenceId, true, newlyDerived,
                "Evidence 已发现，并完成可用 Clue 派生。");
    }

    private List<String> deriveClues(UUID playerId, CaseDefinition definition) {
        List<String> newlyDerived = new ArrayList<>();
        boolean changed;
        do {
            changed = false;
            PlayerCaseInvestigationState state = investigationService.getState(playerId, definition.id());
            for (ClueDefinition clue : definition.clues()) {
                if (state.discoveredClueIds().contains(clue.id())) {
                    continue;
                }
                if (InvestigationRequirements.anySatisfied(
                        clue.derivationRules(),
                        state.discoveredEvidenceIds(),
                        state.discoveredClueIds()
                ) && investigationService.discoverClue(playerId, definition.id(), clue.id())) {
                    newlyDerived.add(clue.id());
                    changed = true;
                    state = investigationService.getState(playerId, definition.id());
                }
            }
        } while (changed);
        return List.copyOf(newlyDerived);
    }

    private static DiscoverEvidenceResult result(
            OperationStatus status,
            String caseId,
            String evidenceId,
            boolean newlyDiscovered,
            List<String> clueIds,
            String message
    ) {
        return new DiscoverEvidenceResult(status, caseId, evidenceId, newlyDiscovered, clueIds, message);
    }
}
