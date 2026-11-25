package com.fs.starfarer.api.impl.campaign.rulecmd;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;
public class IsEnoughRepWithFaction extends BaseCommandPlugin {
    public IsEnoughRepWithFaction() {
    }
    @Override
    public boolean execute(String ruleId,
                           InteractionDialogAPI dialog,
                           List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) return false;

        String factionId = params.get(0).getString(memoryMap);
        if (factionId == null) return false;

        float threshold = params.get(1).getFloat(memoryMap);
        if (threshold < -1.0 || threshold > 1.0) return false;

        float targetFactionRep = Global.getSector().getPlayerFaction().getRelationship(factionId);

        return (targetFactionRep >= threshold);
        //done!
    }
}
