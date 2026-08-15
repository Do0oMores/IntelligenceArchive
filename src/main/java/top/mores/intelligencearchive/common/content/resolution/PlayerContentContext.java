package top.mores.intelligencearchive.common.content.resolution;

import top.mores.intelligencearchive.common.model.investigation.PlayerInvestigationState;

import java.util.Objects;
import java.util.UUID;

/**
 * 一次内容解析所需的最小玩家快照。
 *
 * <p>不传入 Minecraft Player 或 Entity，使 Resolver 可在服务端测试、工具和未来多服环境复用。</p>
 */
public record PlayerContentContext(
        UUID playerId,
        PlayerInvestigationState investigationState
) {
    public PlayerContentContext {
        playerId = Objects.requireNonNull(playerId, "playerId 不能为 null");
        investigationState = Objects.requireNonNull(
                investigationState,
                "investigationState 不能为 null"
        );
        if (!playerId.equals(investigationState.playerId())) {
            throw new IllegalArgumentException("playerId 必须与 investigationState.playerId 一致");
        }
    }
}
