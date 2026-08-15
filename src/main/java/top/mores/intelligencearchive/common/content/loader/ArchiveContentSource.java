package top.mores.intelligencearchive.common.content.loader;

/**
 * 服务端 Markdown 内容来源端口。
 *
 * <p>本阶段只定义读取边界，不实现文件系统或 ResourcePack。未来不同来源只需提供 Markdown 文本，
 * Parser 和 Converter 不需要知道内容来自哪里。</p>
 */
public interface ArchiveContentSource {
    String contentId();

    String documentId();

    String version();

    String readMarkdown();
}
