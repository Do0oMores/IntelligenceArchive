package top.mores.intelligencearchive.common.event;

import java.time.Instant;

/**
 * IntelligenceArchive 已发生业务事实的公共事件契约。
 *
 * <p>事件不是“请求执行操作”的命令；它只能在核心状态修改成功后创建。
 * 事件保持纯 Java，使任务、NPC 或外部适配器未来可以监听事实，而核心逻辑不依赖 Forge。</p>
 */
public interface ArchiveEvent {
    Instant timestamp();
}
