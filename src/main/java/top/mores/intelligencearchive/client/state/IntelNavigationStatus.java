package top.mores.intelligencearchive.client.state;

/** IntelLink 从请求到服务端裁决的客户端展示状态。 */
public enum IntelNavigationStatus {
    IDLE,
    REQUESTING,
    RESOLVED,
    UNKNOWN,
    FAILED
}
