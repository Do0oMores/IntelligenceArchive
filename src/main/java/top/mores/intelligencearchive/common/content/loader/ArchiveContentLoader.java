package top.mores.intelligencearchive.common.content.loader;

import top.mores.intelligencearchive.common.content.ArchiveContent;

/** 从受控内容来源生产 ArchiveContent 的加载端口。 */
public interface ArchiveContentLoader {
    ArchiveContent load(ArchiveContentSource source);
}
