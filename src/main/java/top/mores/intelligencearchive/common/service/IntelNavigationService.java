package top.mores.intelligencearchive.common.service;

import top.mores.intelligencearchive.common.model.IntelNavigationResult;

import java.util.UUID;

/** 服务端解析 IntelLink，并隐藏玩家尚未发现的目标信息。 */
public interface IntelNavigationService {
    IntelNavigationResult resolve(UUID playerId, String targetIntelId);
}
