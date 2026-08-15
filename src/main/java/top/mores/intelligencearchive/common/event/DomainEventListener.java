package top.mores.intelligencearchive.common.event;

/** 纯 Java 事件监听端口；外部系统应通过适配器实现它。 */
@FunctionalInterface
public interface DomainEventListener {
    void onEvent(ArchiveEvent event);
}
