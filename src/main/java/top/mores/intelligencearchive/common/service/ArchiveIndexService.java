package top.mores.intelligencearchive.common.service;

import top.mores.intelligencearchive.common.model.ArchiveSummary;

import java.util.List;
import java.util.UUID;

/** 服务端查询某个玩家当前可见档案摘要的业务边界。 */
public interface ArchiveIndexService {
    List<ArchiveSummary> findVisibleArchives(UUID playerId, int limit);
}
