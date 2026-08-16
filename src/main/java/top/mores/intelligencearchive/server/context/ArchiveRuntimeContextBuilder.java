package top.mores.intelligencearchive.server.context;

import net.minecraft.server.packs.resources.ResourceManager;
import top.mores.intelligencearchive.application.usecase.DiscoverIntelUseCase;
import top.mores.intelligencearchive.application.usecase.ReadArchiveUseCase;
import top.mores.intelligencearchive.application.usecase.ResolveArchiveContentUseCase;
import top.mores.intelligencearchive.application.usecase.VerifyIntelUseCase;
import top.mores.intelligencearchive.application.discovery.DefaultDiscoveryService;
import top.mores.intelligencearchive.application.discovery.EvidenceDiscoveryGateway;
import top.mores.intelligencearchive.common.content.loader.MarkdownContentLoader;
import top.mores.intelligencearchive.common.content.resolution.ArchiveContentResolver;
import top.mores.intelligencearchive.common.content.service.ArchiveContentService;
import top.mores.intelligencearchive.common.event.DomainEventPublisher;
import top.mores.intelligencearchive.common.service.IntelService;
import top.mores.intelligencearchive.common.service.ArchiveIndexService;
import top.mores.intelligencearchive.common.service.ArchiveVisibilityResolver;
import top.mores.intelligencearchive.common.service.IntelNavigationService;
import top.mores.intelligencearchive.common.service.InvestigationService;
import top.mores.intelligencearchive.common.discovery.DiscoveryService;
import top.mores.intelligencearchive.common.casefile.service.CaseDefinitionService;
import top.mores.intelligencearchive.common.casefile.service.CaseInvestigationService;
import top.mores.intelligencearchive.common.investigation.service.InvestigationViewService;
import top.mores.intelligencearchive.server.content.repository.ContentLoadReport;
import top.mores.intelligencearchive.server.content.repository.ResourceArchiveContentRepository;
import top.mores.intelligencearchive.server.event.SimpleDomainEventPublisher;
import top.mores.intelligencearchive.server.investigation.view.DefaultInvestigationViewService;
import top.mores.intelligencearchive.server.service.RepositoryArchiveContentService;
import top.mores.intelligencearchive.server.service.SimpleArchiveContentService;
import top.mores.intelligencearchive.server.service.SimpleIntelService;
import top.mores.intelligencearchive.server.service.SimpleArchiveIndexService;
import top.mores.intelligencearchive.server.service.SimpleIntelNavigationService;
import top.mores.intelligencearchive.server.service.SimpleInvestigationService;
import top.mores.intelligencearchive.server.service.SimpleCaseDefinitionService;
import top.mores.intelligencearchive.server.service.SimpleCaseInvestigationService;

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
    private final EvidenceDiscoveryGateway evidenceDiscoveryGateway;
    private final CaseDefinitionService caseDefinitionService;
    private final CaseInvestigationService caseInvestigationService;
    private final ContentLoadReport contentLoadReport;

    /** 从当前服务器 ResourceManager 建立真实内容 Repository 和 Service。 */
    public ArchiveRuntimeContextBuilder(ResourceManager resourceManager) {
        this(createResourceRepository(resourceManager));
    }

    private ArchiveRuntimeContextBuilder(ResourceArchiveContentRepository repository) {
        this(
                new SimpleIntelService(),
                new SimpleInvestigationService(),
                new RepositoryArchiveContentService(repository),
                new ArchiveContentResolver(),
                new SimpleDomainEventPublisher(),
                Clock.systemUTC(),
                EvidenceDiscoveryGateway.unavailable(),
                new SimpleCaseDefinitionService(List.of()),
                new SimpleCaseInvestigationService(),
                repository.loadReport()
        );
    }

    /** 显式注入构造器用于替换 Repository/Service 实现。 */
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
        this(
                intelService,
                investigationService,
                archiveContentService,
                archiveContentResolver,
                domainEventPublisher,
                clock,
                EvidenceDiscoveryGateway.unavailable()
        );
    }

    /** 允许外围 Case/GameCore 适配器提供 Evidence 定位，同时保持 Discovery 核心不依赖 Case。 */
    public ArchiveRuntimeContextBuilder(
            IntelService intelService,
            InvestigationService investigationService,
            ArchiveContentService archiveContentService,
            ArchiveContentResolver archiveContentResolver,
            DomainEventPublisher domainEventPublisher,
            Clock clock,
            EvidenceDiscoveryGateway evidenceDiscoveryGateway
    ) {
        this(
                intelService,
                investigationService,
                archiveContentService,
                archiveContentResolver,
                domainEventPublisher,
                clock,
                evidenceDiscoveryGateway,
                new SimpleCaseDefinitionService(List.of()),
                new SimpleCaseInvestigationService(),
                ContentLoadReport.empty()
        );
    }

    /** 完整服务器装配可注入 Case 服务，但 Context 仍只持有服务依赖，不保存玩家快照。 */
    public ArchiveRuntimeContextBuilder(
            IntelService intelService,
            InvestigationService investigationService,
            ArchiveContentService archiveContentService,
            ArchiveContentResolver archiveContentResolver,
            DomainEventPublisher domainEventPublisher,
            Clock clock,
            EvidenceDiscoveryGateway evidenceDiscoveryGateway,
            CaseDefinitionService caseDefinitionService,
            CaseInvestigationService caseInvestigationService
    ) {
        this(
                intelService,
                investigationService,
                archiveContentService,
                archiveContentResolver,
                domainEventPublisher,
                clock,
                evidenceDiscoveryGateway,
                caseDefinitionService,
                caseInvestigationService,
                ContentLoadReport.empty()
        );
    }

    private ArchiveRuntimeContextBuilder(
            IntelService intelService,
            InvestigationService investigationService,
            ArchiveContentService archiveContentService,
            ArchiveContentResolver archiveContentResolver,
            DomainEventPublisher domainEventPublisher,
            Clock clock,
            EvidenceDiscoveryGateway evidenceDiscoveryGateway,
            CaseDefinitionService caseDefinitionService,
            CaseInvestigationService caseInvestigationService,
            ContentLoadReport contentLoadReport
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
        this.evidenceDiscoveryGateway = Objects.requireNonNull(
                evidenceDiscoveryGateway,
                "evidenceDiscoveryGateway 不能为 null"
        );
        this.caseDefinitionService = Objects.requireNonNull(
                caseDefinitionService,
                "caseDefinitionService 不能为 null"
        );
        this.caseInvestigationService = Objects.requireNonNull(
                caseInvestigationService,
                "caseInvestigationService 不能为 null"
        );
        this.contentLoadReport = Objects.requireNonNull(
                contentLoadReport,
                "contentLoadReport 不能为 null"
        );
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
        ResolveArchiveContentUseCase resolveArchiveContentUseCase = new ResolveArchiveContentUseCase(
                archiveContentService,
                investigationService,
                archiveContentResolver
        );
        ArchiveIndexService archiveIndexService = new SimpleArchiveIndexService(
                intelService,
                archiveContentService,
                investigationService,
                new ArchiveVisibilityResolver()
        );
        IntelNavigationService intelNavigationService = new SimpleIntelNavigationService(
                intelService,
                archiveContentService,
                investigationService
        );
        DiscoveryService discoveryService = new DefaultDiscoveryService(
                intelService,
                discoverIntelUseCase,
                evidenceDiscoveryGateway,
                domainEventPublisher,
                clock
        );
        InvestigationViewService investigationViewService = new DefaultInvestigationViewService(
                intelService,
                caseDefinitionService,
                caseInvestigationService,
                investigationService,
                clock
        );

        return new ArchiveRuntimeContext(
                intelService,
                investigationService,
                archiveContentService,
                archiveIndexService,
                intelNavigationService,
                discoveryService,
                investigationViewService,
                domainEventPublisher,
                discoverIntelUseCase,
                readArchiveUseCase,
                verifyIntelUseCase,
                resolveArchiveContentUseCase
        );
    }

    public ContentLoadReport getContentLoadReport() {
        return contentLoadReport;
    }

    private static ResourceArchiveContentRepository createResourceRepository(ResourceManager resourceManager) {
        return new ResourceArchiveContentRepository(
                Objects.requireNonNull(resourceManager, "resourceManager 不能为 null"),
                new MarkdownContentLoader()
        );
    }
}
