package top.mores.intelligencearchive.server.content.repository;

import java.io.IOException;
import java.util.List;

/** Repository 的资源读取边界；测试可替换它而无需构造 Minecraft Server。 */
interface ArchiveResourceProvider {
    List<ArchiveResourceKey> findArchives() throws IOException;

    String readMetadata(ArchiveResourceKey key) throws IOException;

    String readMarkdown(ArchiveResourceKey key) throws IOException;
}
