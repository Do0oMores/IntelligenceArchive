package top.mores.intelligencearchive.application.usecase;

import top.mores.intelligencearchive.application.result.CreateInvestigationRelationResult;
import top.mores.intelligencearchive.application.result.OperationStatus;
import top.mores.intelligencearchive.common.casefile.model.CaseDefinition;
import top.mores.intelligencearchive.common.casefile.service.CaseDefinitionService;
import top.mores.intelligencearchive.common.casefile.service.CaseInvestigationService;
import top.mores.intelligencearchive.common.casefile.state.InvestigationConfidence;
import top.mores.intelligencearchive.common.casefile.state.InvestigationRelationType;
import top.mores.intelligencearchive.common.casefile.state.PlayerCaseInvestigationState;
import top.mores.intelligencearchive.common.casefile.state.PlayerInvestigationEdge;
import top.mores.intelligencearchive.common.service.IntelService;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 根据玩家已取得的 Clue 创建主观调查关系。
 *
 * <p>用例只检查输入与调查语义是否合法，绝不拿世界 IntelEdge 判断玩家结论是否正确。</p>
 */
public final class CreateInvestigationRelationUseCase {
    private final CaseDefinitionService definitionService;
    private final CaseInvestigationService investigationService;
    private final IntelService intelService;
    private final Clock clock;
    private final Supplier<String> edgeIdSupplier;

    public CreateInvestigationRelationUseCase(
            CaseDefinitionService definitionService,
            CaseInvestigationService investigationService,
            IntelService intelService
    ) {
        this(definitionService, investigationService, intelService, Clock.systemUTC(),
                () -> UUID.randomUUID().toString());
    }

    public CreateInvestigationRelationUseCase(
            CaseDefinitionService definitionService,
            CaseInvestigationService investigationService,
            IntelService intelService,
            Clock clock,
            Supplier<String> edgeIdSupplier
    ) {
        this.definitionService = Objects.requireNonNull(definitionService, "definitionService 不能为 null");
        this.investigationService = Objects.requireNonNull(investigationService, "investigationService 不能为 null");
        this.intelService = Objects.requireNonNull(intelService, "intelService 不能为 null");
        this.clock = Objects.requireNonNull(clock, "clock 不能为 null");
        this.edgeIdSupplier = Objects.requireNonNull(edgeIdSupplier, "edgeIdSupplier 不能为 null");
    }

    public CreateInvestigationRelationResult execute(
            UUID playerId,
            String caseId,
            String sourceNodeId,
            String targetNodeId,
            InvestigationRelationType relationType,
            InvestigationConfidence confidence,
            Set<String> sourceClueIds
    ) {
        String resultCaseId = UseCaseSupport.resultId(caseId);
        if (playerId == null
                || UseCaseSupport.invalidId(caseId)
                || UseCaseSupport.invalidId(sourceNodeId)
                || UseCaseSupport.invalidId(targetNodeId)
                || sourceNodeId.equals(targetNodeId)
                || relationType == null
                || confidence == null
                || sourceClueIds == null
                || sourceClueIds.isEmpty()) {
            return result(OperationStatus.INVALID_INPUT, resultCaseId, null,
                    "关系输入无效或 source 与 target 相同。");
        }

        CaseDefinition definition = definitionService.findCase(caseId).orElse(null);
        if (definition == null) {
            return result(OperationStatus.CASE_NOT_FOUND, caseId, null, "Case 不存在。");
        }
        if (intelService.findNodeById(sourceNodeId).isEmpty()
                || intelService.findNodeById(targetNodeId).isEmpty()) {
            return result(OperationStatus.NODE_NOT_FOUND, caseId, null, "世界 IntelNode 不存在。");
        }

        PlayerCaseInvestigationState state = investigationService.getState(playerId, caseId);
        for (String clueId : sourceClueIds) {
            if (UseCaseSupport.invalidId(clueId) || definition.findClue(clueId).isEmpty()) {
                return result(OperationStatus.CLUE_NOT_FOUND, caseId, null, "来源 Clue 不属于该 Case。");
            }
            if (!state.discoveredClueIds().contains(clueId)) {
                return result(OperationStatus.CLUE_NOT_DISCOVERED, caseId, null, "来源 Clue 尚未被玩家发现。");
            }
        }

        Instant now = clock.instant();
        PlayerInvestigationEdge edge;
        try {
            edge = new PlayerInvestigationEdge(
                    edgeIdSupplier.get(),
                    sourceNodeId,
                    targetNodeId,
                    relationType,
                    confidence,
                    sourceClueIds,
                    now
            );
        } catch (IllegalArgumentException | NullPointerException exception) {
            return result(OperationStatus.INVALID_INPUT, caseId, null, "关系输入无法构成有效 Edge。");
        }

        if (!investigationService.addInvestigationEdge(playerId, caseId, edge)) {
            return result(OperationStatus.RELATION_ALREADY_EXISTS, caseId, null, "完全相同的调查关系已经存在。");
        }
        return result(OperationStatus.SUCCESS, caseId, edge, "玩家调查关系已创建。");
    }

    private static CreateInvestigationRelationResult result(
            OperationStatus status,
            String caseId,
            PlayerInvestigationEdge edge,
            String message
    ) {
        return new CreateInvestigationRelationResult(status, caseId, Optional.ofNullable(edge), message);
    }
}
