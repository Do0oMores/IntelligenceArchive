package top.mores.intelligencearchive.client.investigation;

/** 客户端调查视图请求生命周期；它不是服务端权限或解锁状态。 */
public enum InvestigationViewLoadStatus {
    IDLE,
    REQUESTING,
    LOADED,
    FAILED
}
