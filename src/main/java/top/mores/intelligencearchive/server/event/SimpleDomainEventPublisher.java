package top.mores.intelligencearchive.server.event;

import top.mores.intelligencearchive.common.event.ArchiveEvent;
import top.mores.intelligencearchive.common.event.DomainEventListener;
import top.mores.intelligencearchive.common.event.DomainEventPublisher;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 服务器会话内使用的轻量纯 Java 发布器。
 *
 * <p>CopyOnWriteArrayList 允许监听期间安全注册或移除监听器。单个监听器异常会被隔离，
 * 不影响后续监听器，更不能回滚已经完成的核心业务状态。</p>
 */
public final class SimpleDomainEventPublisher implements DomainEventPublisher {
    private final CopyOnWriteArrayList<DomainEventListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void publish(ArchiveEvent event) {
        ArchiveEvent validEvent = Objects.requireNonNull(event, "event 不能为 null");
        for (DomainEventListener listener : listeners) {
            try {
                listener.onEvent(validEvent);
            } catch (RuntimeException ignored) {
                // 监听器属于外围扩展，失败不能破坏核心业务或阻止其他监听器接收事实。
            }
        }
    }

    @Override
    public void registerListener(DomainEventListener listener) {
        listeners.addIfAbsent(Objects.requireNonNull(listener, "listener 不能为 null"));
    }

    @Override
    public void unregisterListener(DomainEventListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }
}
