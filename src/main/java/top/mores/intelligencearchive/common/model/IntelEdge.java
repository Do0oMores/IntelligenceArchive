package top.mores.intelligencearchive.common.model;

import java.util.Objects;

/**
 * 一条从 source 到 target 的有向调查关系。
 *
 * <p>边只保存节点 ID，不直接保存 {@link IntelNode} 对象引用。这样未来切换到数据库、
 * 跨缓存加载或按需查询时，不会形成难以持久化的对象图。</p>
 */
public record IntelEdge(
        String sourceNodeId,
        String targetNodeId,
        IntelRelationType relationType
) {
    public IntelEdge {
        sourceNodeId = DomainValidation.requireNonBlank(sourceNodeId, "sourceNodeId");
        targetNodeId = DomainValidation.requireNonBlank(targetNodeId, "targetNodeId");
        relationType = Objects.requireNonNull(relationType, "relationType 不能为 null");
    }
}
