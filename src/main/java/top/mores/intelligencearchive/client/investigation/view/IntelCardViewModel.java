package top.mores.intelligencearchive.client.investigation.view;

import java.util.Objects;

/** 玩家已知 Intel 的卡片数据，不包含描述、隐藏数据或世界关系。 */
public record IntelCardViewModel(String id, String title, String category, String importance) {
    public IntelCardViewModel {
        id = Objects.requireNonNull(id, "id 不能为 null");
        title = Objects.requireNonNull(title, "title 不能为 null");
        category = Objects.requireNonNull(category, "category 不能为 null");
        importance = Objects.requireNonNull(importance, "importance 不能为 null");
    }
}
