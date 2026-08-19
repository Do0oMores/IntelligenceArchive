package top.mores.intelligencearchive.common.investigation.view;

import top.mores.intelligencearchive.common.model.IntelNodeType;

import java.util.Objects;

/**
 * 玩家已经发现的 IntelNode 投影。
 *
 * <p>只暴露 UI 所需显示字段，不携带 IntelNode、世界关系、描述或隐藏 metadata。</p>
 */
public record InvestigationNodeView(
        String intelId,
        String displayName,
        IntelNodeType type,
        InvestigationViewStatus discoveryStatus,
        String importance
) {
    public InvestigationNodeView {
        intelId = InvestigationViewValidation.requireText(intelId, "intelId");
        displayName = InvestigationViewValidation.requireText(displayName, "displayName");
        type = Objects.requireNonNull(type, "type 不能为 null");
        discoveryStatus = Objects.requireNonNull(discoveryStatus, "discoveryStatus 不能为 null");
        importance = InvestigationViewValidation.requireText(importance, "importance");
    }
}
