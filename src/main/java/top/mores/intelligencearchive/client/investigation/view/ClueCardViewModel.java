package top.mores.intelligencearchive.client.investigation.view;

import java.util.Objects;

/** 已形成 Clue 的卡片数据，不暴露来源 Evidence 或派生规则。 */
public record ClueCardViewModel(String id, String title, String importance, String reliability) {
    public ClueCardViewModel {
        id = Objects.requireNonNull(id, "id 不能为 null");
        title = Objects.requireNonNull(title, "title 不能为 null");
        importance = Objects.requireNonNull(importance, "importance 不能为 null");
        reliability = Objects.requireNonNull(reliability, "reliability 不能为 null");
    }
}
