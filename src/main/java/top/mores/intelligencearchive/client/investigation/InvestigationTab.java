package top.mores.intelligencearchive.client.investigation;

import net.minecraft.network.chat.Component;

/** 调查终端第一阶段的固定信息分类；这里不是任务阶段或案件流程。 */
public enum InvestigationTab {

    CASE(
            "gui.intelligencearchive.tab.case"
    ),

    INTEL(
            "gui.intelligencearchive.tab.intel"
    ),

    EVIDENCE(
            "gui.intelligencearchive.tab.evidence"
    ),

    CLUES(
            "gui.intelligencearchive.tab.clues"
    ),

    HYPOTHESIS(
            "gui.intelligencearchive.tab.hypothesis"
    );


    private final String translationKey;

    InvestigationTab(String translationKey) {
        this.translationKey = translationKey;
    }


    public Component displayName() {
        return Component.translatable(translationKey);
    }
}
