package top.mores.intelligencearchive.client.render;

/** IntelLink 渲染器的点击回调；回调只提交目标 ID，导航可见性仍由服务端裁决。 */
@FunctionalInterface
public interface ArchiveLinkClickHandler {
    void onIntelLinkClick(String intelId);
}
