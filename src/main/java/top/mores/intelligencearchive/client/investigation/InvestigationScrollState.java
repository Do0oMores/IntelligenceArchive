package top.mores.intelligencearchive.client.investigation;

/**
 * 调查终端内容区的独立滚动状态。
 *
 * <p>它只管理像素偏移和视口尺寸，不感知 DTO、卡片类型或业务数据。</p>
 */
public final class InvestigationScrollState {
    private double offset;
    private int contentHeight;
    private int viewportHeight;

    public void updateMetrics(int contentHeight, int viewportHeight) {
        this.contentHeight = Math.max(0, contentHeight);
        this.viewportHeight = Math.max(0, viewportHeight);
        clamp();
    }

    public boolean scrollBy(double pixels) {
        double oldOffset = offset;
        offset += pixels;
        clamp();
        return Double.compare(oldOffset, offset) != 0;
    }

    public void scrollToStart() {
        offset = 0.0D;
    }

    public void scrollToEnd() {
        offset = maxOffset();
    }

    public void reset() {
        offset = 0.0D;
        contentHeight = 0;
        viewportHeight = 0;
    }

    public double offset() {
        return offset;
    }

    public int contentHeight() {
        return contentHeight;
    }

    public int viewportHeight() {
        return viewportHeight;
    }

    public double maxOffset() {
        return Math.max(0, contentHeight - viewportHeight);
    }

    public boolean canScroll() {
        return contentHeight > viewportHeight;
    }

    private void clamp() {
        offset = Math.max(0.0D, Math.min(offset, maxOffset()));
    }
}
