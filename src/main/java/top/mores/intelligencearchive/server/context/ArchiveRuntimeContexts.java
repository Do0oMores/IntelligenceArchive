package top.mores.intelligencearchive.server.context;

import java.util.Objects;
import java.util.Optional;

/**
 * 当前服务器会话 Context 的最小生命周期持有器。
 *
 * <p>静态引用只保存不可变依赖容器，不保存玩家业务状态；服务器启动时安装一次，停止时清除，
 * 防止集成服务器在同一 JVM 中重新开服时复用上一会话的内存状态。</p>
 */
public final class ArchiveRuntimeContexts {
    private static volatile ArchiveRuntimeContext current;

    private ArchiveRuntimeContexts() {
    }

    public static synchronized void install(ArchiveRuntimeContext context) {
        if (current != null) {
            throw new IllegalStateException("ArchiveRuntimeContext 已经初始化");
        }
        current = Objects.requireNonNull(context, "context 不能为 null");
    }

    public static Optional<ArchiveRuntimeContext> current() {
        return Optional.ofNullable(current);
    }

    public static synchronized void clear() {
        current = null;
    }
}
