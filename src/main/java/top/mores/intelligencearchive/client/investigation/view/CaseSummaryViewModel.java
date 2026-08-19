package top.mores.intelligencearchive.client.investigation.view;

import java.util.Objects;

/** 服务端提供的案件展示上下文；客户端不得根据 caseId 猜测标题或状态。 */
public record CaseSummaryViewModel(String caseId, String title, String status) {
    public CaseSummaryViewModel {
        caseId = Objects.requireNonNull(caseId, "caseId 不能为 null");
        title = Objects.requireNonNull(title, "title 不能为 null");
        status = Objects.requireNonNull(status, "status 不能为 null");
    }
}
