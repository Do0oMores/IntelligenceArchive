package top.mores.intelligencearchive.common.model;

import java.time.Instant;
import java.util.Objects;

/**
 * 档案的基础元数据。
 *
 * <p>这里只保留当前已经明确需要的创建时间、作者和安全等级，避免在领域尚未稳定时
 * 预先堆积大量可空字段。安全等级也不能替代未来服务端的玩家权限判断。</p>
 */
public record ArchiveMetadata(
        Instant createdTime,
        String author,
        ArchiveSecurityLevel securityLevel
) {
    public ArchiveMetadata {
        createdTime = Objects.requireNonNull(createdTime, "createdTime 不能为 null");
        author = DomainValidation.requireNonBlank(author, "author");
        securityLevel = Objects.requireNonNull(securityLevel, "securityLevel 不能为 null");
    }
}
