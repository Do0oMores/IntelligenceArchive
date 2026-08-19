package top.mores.intelligencearchive.client.state;

import java.util.Objects;

/** 与 Minecraft GUI 解耦的简单页面导航状态，便于验证索引到详情的转换。 */
public final class ArchiveTerminalState {
    private ArchiveTerminalPage page = ArchiveTerminalPage.INDEX;
    private String selectedDocumentId = "";

    public void openDocument(String documentId) {
        Objects.requireNonNull(documentId, "documentId 不能为 null");
        if (documentId.isBlank()) {
            throw new IllegalArgumentException("documentId 不能为空");
        }
        selectedDocumentId = documentId;
        page = ArchiveTerminalPage.DETAIL;
    }

    public void showIndex() {
        selectedDocumentId = "";
        page = ArchiveTerminalPage.INDEX;
    }

    public ArchiveTerminalPage page() {
        return page;
    }

    public String selectedDocumentId() {
        return selectedDocumentId;
    }
}
