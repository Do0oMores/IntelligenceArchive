package top.mores.intelligencearchive.client.view;

/** Screen Renderer 消费的展示节点，不包含网络或服务端 Domain 对象。 */
public interface ArchiveViewNode {
    ArchiveViewNodeType type();
}
