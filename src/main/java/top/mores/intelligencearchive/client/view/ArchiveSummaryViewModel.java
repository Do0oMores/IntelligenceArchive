package top.mores.intelligencearchive.client.view;

import top.mores.intelligencearchive.common.model.ArchiveSummaryStatus;

import java.util.Objects;

/** ArchiveScreen 索引页消费的不可变展示模型。 */
public record ArchiveSummaryViewModel(
        String documentId,
        String title,
        String type,
        String securityLevel,
        String summary,
        ArchiveSummaryStatus status,
        String version
) {
    public ArchiveSummaryViewModel {
        Objects.requireNonNull(documentId, "documentId 不能为 null");
        Objects.requireNonNull(title, "title 不能为 null");
        Objects.requireNonNull(type, "type 不能为 null");
        Objects.requireNonNull(securityLevel, "securityLevel 不能为 null");
        Objects.requireNonNull(summary, "summary 不能为 null");
        Objects.requireNonNull(status, "status 不能为 null");
        Objects.requireNonNull(version, "version 不能为 null");
    }
}
