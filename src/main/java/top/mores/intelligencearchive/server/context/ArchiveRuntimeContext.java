package top.mores.intelligencearchive.server.context;

import top.mores.intelligencearchive.application.usecase.DiscoverIntelUseCase;
import top.mores.intelligencearchive.application.usecase.ReadArchiveUseCase;
import top.mores.intelligencearchive.application.usecase.ResolveArchiveContentUseCase;
import top.mores.intelligencearchive.application.usecase.VerifyIntelUseCase;
import top.mores.intelligencearchive.common.content.service.ArchiveContentService;
import top.mores.intelligencearchive.common.event.DomainEventPublisher;
import top.mores.intelligencearchive.common.service.IntelService;
import top.mores.intelligencearchive.common.service.ArchiveIndexService;
import top.mores.intelligencearchive.common.service.IntelNavigationService;
import top.mores.intelligencearchive.common.service.InvestigationService;
import top.mores.intelligencearchive.common.discovery.DiscoveryService;
import top.mores.intelligencearchive.common.investigation.service.InvestigationViewService;

import java.util.Objects;

/**
 * 单个 Minecraft 服务器会话的不可变依赖容器。
 *
 * <p>Context 让 Packet、未来任务适配器和其他入口共享同一组 Service/UseCase 实例，
 * 避免各模块自行 new 服务导致玩家状态分裂。它只保存引用，不负责创建对象，也不提供 setter。</p>
 */
public final class ArchiveRuntimeContext {
    private final IntelService intelService;
    private final InvestigationService investigationService;
    private final ArchiveContentService archiveContentService;
    private final ArchiveIndexService archiveIndexService;
    private final IntelNavigationService intelNavigationService;
    private final DiscoveryService discoveryService;
    private final InvestigationViewService investigationViewService;
    private final DomainEventPublisher domainEventPublisher;
    private final DiscoverIntelUseCase discoverIntelUseCase;
    private final ReadArchiveUseCase readArchiveUseCase;
    private final VerifyIntelUseCase verifyIntelUseCase;
    private final ResolveArchiveContentUseCase resolveArchiveContentUseCase;

    ArchiveRuntimeContext(
            IntelService intelService,
            InvestigationService investigationService,
            ArchiveContentService archiveContentService,
            ArchiveIndexService archiveIndexService,
            IntelNavigationService intelNavigationService,
            DiscoveryService discoveryService,
            InvestigationViewService investigationViewService,
            DomainEventPublisher domainEventPublisher,
            DiscoverIntelUseCase discoverIntelUseCase,
            ReadArchiveUseCase readArchiveUseCase,
            VerifyIntelUseCase verifyIntelUseCase,
            ResolveArchiveContentUseCase resolveArchiveContentUseCase
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
        this.archiveIndexService = Objects.requireNonNull(archiveIndexService, "archiveIndexService 不能为 null");
        this.intelNavigationService = Objects.requireNonNull(
                intelNavigationService,
                "intelNavigationService 不能为 null"
        );
        this.discoveryService = Objects.requireNonNull(discoveryService, "discoveryService 不能为 null");
        this.investigationViewService = Objects.requireNonNull(
                investigationViewService,
                "investigationViewService 不能为 null"
        );
        this.domainEventPublisher = Objects.requireNonNull(
                domainEventPublisher,
                "domainEventPublisher 不能为 null"
        );
        this.discoverIntelUseCase = Objects.requireNonNull(
                discoverIntelUseCase,
                "discoverIntelUseCase 不能为 null"
        );
        this.readArchiveUseCase = Objects.requireNonNull(readArchiveUseCase, "readArchiveUseCase 不能为 null");
        this.verifyIntelUseCase = Objects.requireNonNull(
                verifyIntelUseCase,
                "verifyIntelUseCase 不能为 null"
        );
        this.resolveArchiveContentUseCase = Objects.requireNonNull(
                resolveArchiveContentUseCase,
                "resolveArchiveContentUseCase 不能为 null"
        );
    }

    public IntelService getIntelService() {
        return intelService;
    }

    public InvestigationService getInvestigationService() {
        return investigationService;
    }

    public ArchiveContentService getArchiveContentService() {
        return archiveContentService;
    }

    public ArchiveIndexService getArchiveIndexService() {
        return archiveIndexService;
    }

    public IntelNavigationService getIntelNavigationService() {
        return intelNavigationService;
    }

    /** 外部游戏系统应通过该边界发现情报，而不是直接修改 InvestigationService。 */
    public DiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    /** 未来网络与 UI 只能读取该服务生成的玩家认知投影，不能直接组合 Domain 状态。 */
    public InvestigationViewService getInvestigationViewService() {
        return investigationViewService;
    }

    public DomainEventPublisher getDomainEventPublisher() {
        return domainEventPublisher;
    }

    public DiscoverIntelUseCase getDiscoverIntelUseCase() {
        return discoverIntelUseCase;
    }

    public ReadArchiveUseCase getReadArchiveUseCase() {
        return readArchiveUseCase;
    }

    public VerifyIntelUseCase getVerifyIntelUseCase() {
        return verifyIntelUseCase;
    }

    public ResolveArchiveContentUseCase getResolveArchiveContentUseCase() {
        return resolveArchiveContentUseCase;
    }
}
