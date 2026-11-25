package com.fs.starfarer.api.impl.campaign.rulecmd;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ChornaSystem_modPlugin;

import java.util.List;
import java.util.Map;

public class SetStartWithoutAColony extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId,
                           InteractionDialogAPI dialog,
                           List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) return false;

        ChornaSystem_modPlugin.startWithAColony = false;
        //done!
        return true;
    }
}
