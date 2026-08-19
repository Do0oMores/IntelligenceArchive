package top.mores.intelligencearchive.client.investigation.view;

import java.util.Objects;

/** 玩家当前 Hypothesis 的展示数据，不包含验证条件、真相或正确答案。 */
public record HypothesisCardViewModel(String id, String title, String status, String confidence) {
    public HypothesisCardViewModel {
        id = Objects.requireNonNull(id, "id 不能为 null");
        title = Objects.requireNonNull(title, "title 不能为 null");
        status = Objects.requireNonNull(status, "status 不能为 null");
        confidence = Objects.requireNonNull(confidence, "confidence 不能为 null");
    }
}
