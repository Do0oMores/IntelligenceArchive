package top.mores.intelligencearchive.server.context;

import top.mores.intelligencearchive.application.usecase.DiscoverIntelUseCase;
import top.mores.intelligencearchive.application.usecase.LinkIntelUseCase;
import top.mores.intelligencearchive.application.usecase.ReadArchiveUseCase;
import top.mores.intelligencearchive.application.usecase.ResolveArchiveContentUseCase;
import top.mores.intelligencearchive.application.usecase.VerifyIntelUseCase;
import top.mores.intelligencearchive.common.content.ArchiveContent;
import top.mores.intelligencearchive.common.content.loader.MarkdownContentLoader;
import top.mores.intelligencearchive.common.content.loader.StringArchiveContentSource;
import top.mores.intelligencearchive.common.content.resolution.ArchiveContentResolver;
import top.mores.intelligencearchive.common.content.service.ArchiveContentService;
import top.mores.intelligencearchive.common.event.DomainEventPublisher;
import top.mores.intelligencearchive.common.service.IntelService;
import top.mores.intelligencearchive.common.service.InvestigationService;
import top.mores.intelligencearchive.server.event.SimpleDomainEventPublisher;
import top.mores.intelligencearchive.server.service.SimpleArchiveContentService;
import top.mores.intelligencearchive.server.service.SimpleIntelService;
import top.mores.intelligencearchive.server.service.SimpleInvestigationService;

import java.time.Clock;
import java.util.List;
import java.util.Objects;

/**
 * Runtime Context 的唯一装配位置。
 *
 * <p>Builder 按 Repository/Service → UseCase → Context 的方向组装依赖；Context 自身只是容器，
 * 因此未来替换持久化或增加服务时不会把工厂逻辑扩散到 Packet 和生命周期事件。</p>
 */
public final class ArchiveRuntimeContextBuilder {
    private final IntelService intelService;
    private final InvestigationService investigationService;
    private final ArchiveContentService archiveContentService;
    private final ArchiveContentResolver archiveContentResolver;
    private final DomainEventPublisher domainEventPublisher;
    private final Clock clock;

    /** 创建当前 Phase 2 使用的默认服务器会话依赖。 */
    public ArchiveRuntimeContextBuilder() {
        this(
                new SimpleIntelService(),
                new SimpleInvestigationService(),
                createDefaultContentService(),
                new ArchiveContentResolver(),
                new SimpleDomainEventPublisher(),
                Clock.systemUTC()
        );
    }

    /** 显式注入构造器用于测试，以及未来替换 Repository/Service 实现。 */
    public ArchiveRuntimeContextBuilder(
            IntelService intelService,
            InvestigationService investigationService,
            DomainEventPublisher domainEventPublisher,
            Clock clock
    ) {
        this(
                intelService,
                investigationService,
                new SimpleArchiveContentService(List.of()),
                new ArchiveContentResolver(),
                domainEventPublisher,
                clock
        );
    }

    /** 完整装配构造器允许替换 ContentService 与条件解析策略。 */
    public ArchiveRuntimeContextBuilder(
            IntelService intelService,
            InvestigationService investigationService,
            ArchiveContentService archiveContentService,
            ArchiveContentResolver archiveContentResolver,
            DomainEventPublisher domainEventPublisher,
            Clock clock
    ) {
        this.intelService = Objects.requireNonNull(intelService, "intelService 不能为 null");
        this.investigationService = Objects.requireNonNull(
                investigationService,
                "investigationService 不能为 null"
        );
        this.archiveContentService = Objects.requireNonNull(
                archiveContentService,
                "archiveContentService 不能为 null"
        );
        this.archiveContentResolver = Objects.requireNonNull(
                archiveContentResolver,
                "archiveContentResolver 不能为 null"
        );
        this.domainEventPublisher = Objects.requireNonNull(
                domainEventPublisher,
                "domainEventPublisher 不能为 null"
        );
        this.clock = Objects.requireNonNull(clock, "clock 不能为 null");
    }

    public ArchiveRuntimeContext build() {
        DiscoverIntelUseCase discoverIntelUseCase = new DiscoverIntelUseCase(
                intelService,
                investigationService,
                domainEventPublisher,
                clock
        );
        ReadArchiveUseCase readArchiveUseCase = new ReadArchiveUseCase(
                intelService,
                investigationService,
                domainEventPublisher,
                clock
        );
        VerifyIntelUseCase verifyIntelUseCase = new VerifyIntelUseCase(
                intelService,
                investigationService,
                domainEventPublisher,
                clock
        );
        LinkIntelUseCase linkIntelUseCase = new LinkIntelUseCase(intelService);
        ResolveArchiveContentUseCase resolveArchiveContentUseCase = new ResolveArchiveContentUseCase(
                intelService,
                archiveContentService,
                investigationService,
                archiveContentResolver
        );

        return new ArchiveRuntimeContext(
                intelService,
                investigationService,
                archiveContentService,
                domainEventPublisher,
                discoverIntelUseCase,
                readArchiveUseCase,
                verifyIntelUseCase,
                linkIntelUseCase,
                resolveArchiveContentUseCase
        );
    }

    /** 为开发环境装载与 Phase 2 测试档案对应的服务端 Markdown 内容。 */
    private static ArchiveContentService createDefaultContentService() {
        ArchiveContent testContent = new MarkdownContentLoader().load(new StringArchiveContentSource(
                "content.case.test_001.v1",
                SimpleIntelService.TEST_DOCUMENT_ID,
                "v1",
                """
                        # 测试档案正文
                        这是由服务端解析并按玩家状态生成的 Resolved Archive Content。
                        ![测试图片](archive/image/test_001.png)
                        [audio:archive/audio/test_001.ogg]
                        [intel:node.location.test_lab]
                        [redacted condition="security.level3"]
                        该隐藏原文不会进入客户端内容。
                        [/redacted]
                        """
        ));
        return new SimpleArchiveContentService(List.of(testContent));
    }
}
