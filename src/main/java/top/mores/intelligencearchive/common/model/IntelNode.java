package top.mores.intelligencearchive.common.model;

import java.util.Objects;

/**
 * 调查图谱中的独立认知节点。
 *
 * <p>节点不等同于档案：玩家可以先得知某个人物或地点，但尚未获得描述它的正式文件。</p>
 */
public record IntelNode(
        String id,
        String name,
        IntelNodeType type,
        String description
) {
    public IntelNode {
        id = DomainValidation.requireNonBlank(id, "id");
        name = DomainValidation.requireNonBlank(name, "name");
        type = Objects.requireNonNull(type, "type 不能为 null");
        description = DomainValidation.requireText(description, "description");
    }
}
