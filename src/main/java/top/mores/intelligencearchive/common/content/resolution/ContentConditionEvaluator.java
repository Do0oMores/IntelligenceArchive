package top.mores.intelligencearchive.common.content.resolution;

/**
 * 内容条件判断端口。
 *
 * <p>Resolver 只依赖该接口，不解析 security、story 或 permission 字符串。未来剧情、任务和权限
 * 规则可替换实现，而无需修改内容遍历逻辑。</p>
 */
@FunctionalInterface
public interface ContentConditionEvaluator {
    boolean evaluate(String conditionReference, PlayerContentContext context);
}
