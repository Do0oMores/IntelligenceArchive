package top.mores.intelligencearchive.common.event;

/**
 * 应用层发布已发生业务事实的端口。
 *
 * <p>接口不引用 Forge EventBus、Bukkit Event 或消息队列，未来基础设施实现可以替换，
 * Application UseCase 无需感知具体分发机制。</p>
 */
public interface DomainEventPublisher {
    DomainEventPublisher NO_OP = new DomainEventPublisher() {
        @Override
        public void publish(ArchiveEvent event) {
        }

        @Override
        public void registerListener(DomainEventListener listener) {
        }

        @Override
        public void unregisterListener(DomainEventListener listener) {
        }
    };

    void publish(ArchiveEvent event);

    void registerListener(DomainEventListener listener);

    void unregisterListener(DomainEventListener listener);

    /** 为不需要事件观察的测试或兼容构造器提供无状态发布器。 */
    static DomainEventPublisher noOp() {
        return NO_OP;
    }
}
