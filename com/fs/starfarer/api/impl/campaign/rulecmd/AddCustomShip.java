package com.fs.starfarer.api.impl.campaign.rulecmd;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;

import java.util.List;
import java.util.Map;
public class AddCustomShip extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId,
                           InteractionDialogAPI dialog,
                           List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) return false;

        String vid = params.get(0).getString(memoryMap);
        ShipVariantAPI variant = Global.getSettings().getVariant(vid);
        Global.getSector().getPlayerFleet().getFleetData().addFleetMember(vid);
        AddRemoveCommodity.addFleetMemberGainText(variant, dialog.getTextPanel());
        //done!
        return true;
    }
}