package top.mores.intelligencearchive.client.render;

/** IntelLink 渲染器的点击回调；Phase 3-C-2 不在这里执行解锁或导航业务。 */
@FunctionalInterface
public interface ArchiveLinkClickHandler {
    void onIntelLinkClick(String intelId);
}
