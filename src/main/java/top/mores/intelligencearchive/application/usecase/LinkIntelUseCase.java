package top.mores.intelligencearchive.application.usecase;

import top.mores.intelligencearchive.application.result.LinkIntelResult;
import top.mores.intelligencearchive.application.result.OperationStatus;
import top.mores.intelligencearchive.common.model.IntelRelationType;
import top.mores.intelligencearchive.common.service.IntelService;

import java.util.Objects;
import java.util.UUID;

/**
 * 玩家建立调查关联的应用入口。
 *
 * <p>Phase 2-D 只验证两个世界节点及输入，不写 Repository，也不保存玩家自定义关系。
 * 后续推理系统可以在不改变调用入口语义的前提下扩展持久化和权限规则。</p>
 */
public final class LinkIntelUseCase {
    private final IntelService intelService;

    public LinkIntelUseCase(IntelService intelService) {
        this.intelService = Objects.requireNonNull(intelService, "intelService 不能为 null");
    }

    public LinkIntelResult execute(
            UUID playerId,
            String sourceIntelId,
            String targetIntelId,
            IntelRelationType relationType
    ) {
        String resultSourceId = UseCaseSupport.resultId(sourceIntelId);
        String resultTargetId = UseCaseSupport.resultId(targetIntelId);
        String resultRelationType = relationType == null ? "" : relationType.name();
        if (playerId == null
                || UseCaseSupport.invalidId(sourceIntelId)
                || UseCaseSupport.invalidId(targetIntelId)
                || relationType == null
                || sourceIntelId.equals(targetIntelId)) {
            return new LinkIntelResult(
                    OperationStatus.INVALID_INPUT,
                    resultSourceId,
                    resultTargetId,
                    resultRelationType,
                    "Player, two different node IDs and relation type are required."
            );
        }

        if (intelService.findNodeById(sourceIntelId).isEmpty()
                || intelService.findNodeById(targetIntelId).isEmpty()) {
            return new LinkIntelResult(
                    OperationStatus.INTEL_NOT_FOUND,
                    sourceIntelId,
                    targetIntelId,
                    resultRelationType,
                    "Both world intel nodes must exist."
            );
        }

        return new LinkIntelResult(
                OperationStatus.SUCCESS,
                sourceIntelId,
                targetIntelId,
                resultRelationType,
                "Intel link validated; Phase 2-D does not persist player links."
        );
    }
}
