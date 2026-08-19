package top.mores.intelligencearchive.client.investigation.view;

import java.util.Objects;

/** 已发现 Evidence 的卡片数据，不包含发现条件或隐藏规则。 */
public record EvidenceCardViewModel(String id, String title, String sourceType, String importance) {
    public EvidenceCardViewModel {
        id = Objects.requireNonNull(id, "id 不能为 null");
        title = Objects.requireNonNull(title, "title 不能为 null");
        sourceType = Objects.requireNonNull(sourceType, "sourceType 不能为 null");
        importance = Objects.requireNonNull(importance, "importance 不能为 null");
    }
}
