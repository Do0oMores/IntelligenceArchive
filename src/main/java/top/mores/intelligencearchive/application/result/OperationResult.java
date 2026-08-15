package top.mores.intelligencearchive.application.result;

/**
 * 所有应用用例结果的统一只读契约。
 *
 * <p>明确结果码和消息比单独返回 boolean 更适合未来的 UI、任务或桥接层；
 * 具体结果 record 还能携带状态变化，而不会让调用方依赖异常控制正常业务流程。</p>
 */
public interface OperationResult {
    OperationStatus status();

    String message();

    default boolean success() {
        return status() == OperationStatus.SUCCESS;
    }
}
