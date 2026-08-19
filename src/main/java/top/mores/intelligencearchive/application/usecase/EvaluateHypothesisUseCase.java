package top.mores.intelligencearchive.application.usecase;

import top.mores.intelligencearchive.application.result.EvaluateHypothesisResult;
import top.mores.intelligencearchive.application.result.OperationStatus;
import top.mores.intelligencearchive.common.casefile.model.CaseDefinition;
import top.mores.intelligencearchive.common.casefile.model.ClueDefinition;
import top.mores.intelligencearchive.common.casefile.model.HypothesisDefinition;
import top.mores.intelligencearchive.common.casefile.model.InvestigationRequirements;
import top.mores.intelligencearchive.common.casefile.service.CaseDefinitionService;
import top.mores.intelligencearchive.common.casefile.service.CaseInvestigationService;
import top.mores.intelligencearchive.common.casefile.state.HypothesisStatus;
import top.mores.intelligencearchive.common.casefile.state.PlayerCaseInvestigationState;

import java.util.Objects;
import java.util.UUID;

/** 玩家显式请求时评估一个预定义 Hypothesis，不返回任何缺失条件或隐藏答案。 */
public final class EvaluateHypothesisUseCase {
    private final CaseDefinitionService definitionService;
    private final CaseInvestigationService investigationService;

    public EvaluateHypothesisUseCase(
            CaseDefinitionService definitionService,
            CaseInvestigationService investigationService
    ) {
        this.definitionService = Objects.requireNonNull(definitionService, "definitionService 不能为 null");
        this.investigationService = Objects.requireNonNull(investigationService, "investigationService 不能为 null");
    }

    public EvaluateHypothesisResult execute(UUID playerId, String caseId, String hypothesisId) {
        String resultCaseId = UseCaseSupport.resultId(caseId);
        String resultHypothesisId = UseCaseSupport.resultId(hypothesisId);
        if (playerId == null || UseCaseSupport.invalidId(caseId)
                || UseCaseSupport.invalidId(hypothesisId)) {
            return result(OperationStatus.INVALID_INPUT, resultCaseId, resultHypothesisId,
                    HypothesisStatus.UNTESTED, HypothesisStatus.UNTESTED, "评估输入无效。");
        }

        CaseDefinition definition = definitionService.findCase(caseId).orElse(null);
        if (definition == null) {
            return result(OperationStatus.CASE_NOT_FOUND, caseId, hypothesisId,
                    HypothesisStatus.UNTESTED, HypothesisStatus.UNTESTED, "Case 不存在。");
        }
        HypothesisDefinition hypothesis = definition.findHypothesis(hypothesisId).orElse(null);
        if (hypothesis == null) {
            return result(OperationStatus.HYPOTHESIS_NOT_FOUND, caseId, hypothesisId,
                    HypothesisStatus.UNTESTED, HypothesisStatus.UNTESTED, "Hypothesis 不属于该 Case。");
        }

        PlayerCaseInvestigationState state = investigationService.getState(playerId, caseId);
        HypothesisStatus oldStatus = state.hypothesisStatus(hypothesisId);
        if (!hypothesis.availabilityRequirements().isEmpty()
                && !InvestigationRequirements.anySatisfied(
                hypothesis.availabilityRequirements(),
                state.discoveredEvidenceIds(),
                state.discoveredClueIds())) {
            return result(OperationStatus.HYPOTHESIS_UNAVAILABLE, caseId, hypothesisId,
                    oldStatus, oldStatus, "Hypothesis 当前不可评估。");
        }

        boolean confirmationSatisfied = InvestigationRequirements.anySatisfied(
                hypothesis.confirmationRequirements(), state.discoveredEvidenceIds(), state.discoveredClueIds());
        boolean refutationSatisfied = InvestigationRequirements.anySatisfied(
                hypothesis.refutationRequirements(), state.discoveredEvidenceIds(), state.discoveredClueIds());
        if (confirmationSatisfied && refutationSatisfied) {
            return result(OperationStatus.DEFINITION_CONFLICT, caseId, hypothesisId,
                    oldStatus, oldStatus, "Hypothesis 的确认与推翻条件同时满足。");
        }

        HypothesisStatus newStatus;
        if (refutationSatisfied) {
            newStatus = HypothesisStatus.REFUTED;
        } else if (confirmationSatisfied) {
            newStatus = HypothesisStatus.CONFIRMED;
        } else {
            boolean supportSignal = InvestigationRequirements.anySatisfied(
                    hypothesis.supportRequirements(), state.discoveredEvidenceIds(), state.discoveredClueIds())
                    || hasClueSignal(definition, state, hypothesisId, true);
            boolean contradictionSignal = hasClueSignal(definition, state, hypothesisId, false);
            if (supportSignal && contradictionSignal) {
                newStatus = HypothesisStatus.DISPUTED;
            } else if (supportSignal) {
                newStatus = HypothesisStatus.SUPPORTED;
            } else {
                newStatus = HypothesisStatus.UNTESTED;
            }
        }

        investigationService.updateHypothesisStatus(playerId, caseId, hypothesisId, newStatus);
        return result(OperationStatus.SUCCESS, caseId, hypothesisId, oldStatus, newStatus,
                "Hypothesis 状态已评估。");
    }

    private static boolean hasClueSignal(
            CaseDefinition definition,
            PlayerCaseInvestigationState state,
            String hypothesisId,
            boolean support
    ) {
        for (String clueId : state.discoveredClueIds()) {
            ClueDefinition clue = definition.findClue(clueId).orElse(null);
            if (clue == null) {
                continue;
            }
            if (support && clue.supportsHypothesisIds().contains(hypothesisId)) {
                return true;
            }
            if (!support && clue.contradictsHypothesisIds().contains(hypothesisId)) {
                return true;
            }
        }
        return false;
    }

    private static EvaluateHypothesisResult result(
            OperationStatus status,
            String caseId,
            String hypothesisId,
            HypothesisStatus oldStatus,
            HypothesisStatus newStatus,
            String message
    ) {
        return new EvaluateHypothesisResult(status, caseId, hypothesisId, oldStatus, newStatus, message);
    }
}
