package top.mores.intelligencearchive.server.integration.casefile;

import java.util.Optional;

/**
 * Evidence 全局 ID 到 Case 上下文的外围定位端口。
 *
 * <p>Discovery API 不接收 caseId；具体 Case 内容提供器负责保证 Evidence ID 可唯一定位。</p>
 */
@FunctionalInterface
public interface CaseEvidenceLocator {
    Optional<String> findCaseId(String evidenceId);
}
