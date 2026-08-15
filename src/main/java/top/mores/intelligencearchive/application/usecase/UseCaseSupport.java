package top.mores.intelligencearchive.application.usecase;

import top.mores.intelligencearchive.common.event.ArchiveEvent;
import top.mores.intelligencearchive.common.event.DomainEventPublisher;
import top.mores.intelligencearchive.common.service.IntelService;

/** 应用用例共享的输入和世界对象检查，不包含状态或存储。 */
final class UseCaseSupport {
    private UseCaseSupport() {
    }

    static boolean invalidId(String value) {
        return value == null || value.isBlank();
    }

    static String resultId(String value) {
        return value == null ? "" : value;
    }

    static boolean worldIntelExists(IntelService intelService, String intelId) {
        return intelService.findDocumentById(intelId).isPresent()
                || intelService.findNodeById(intelId).isPresent();
    }

    /** 外围监听失败不能改变已经提交的调查状态或核心业务结果。 */
    static void publishSafely(DomainEventPublisher publisher, ArchiveEvent event) {
        try {
            publisher.publish(event);
        } catch (RuntimeException ignored) {
            // 发布器是可替换边界；具体基础设施可自行记录失败，Application 保持纯 Java。
        }
    }
}
