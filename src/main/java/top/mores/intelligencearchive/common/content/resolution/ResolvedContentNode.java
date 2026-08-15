package top.mores.intelligencearchive.common.content.resolution;

/**
 * 已完成服务端可见性决策的玩家视角节点。
 *
 * <p>它不能复用世界侧 ContentNode：世界节点可以包含条件引用，而 Resolved 节点只能包含
 * 当前玩家允许看到的表现数据。Renderer 只渲染本接口，不参与权限或隐藏逻辑判断。</p>
 */
public interface ResolvedContentNode {
    ResolvedContentNodeType type();
}
