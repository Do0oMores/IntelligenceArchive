package top.mores.intelligencearchive.client.state;

/** Archive Terminal 索引请求的短生命周期客户端状态。 */
public enum ArchiveIndexLoadStatus {
    IDLE,
    REQUESTING,
    LOADED,
    FAILED
}
