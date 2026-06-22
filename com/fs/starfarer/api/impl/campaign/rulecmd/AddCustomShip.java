package com.fs.starfarer.api.impl.campaign.rulecmd;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeDataForSubmarket;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
public class AddCustomShip extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId,
                           InteractionDialogAPI dialog,
                           List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) return false;
        SectorEntityToken target = (SectorEntityToken) dialog.getInteractionTarget();

        String vid = params.get(0).getString(memoryMap);
        ShipVariantAPI variant = Global.getSettings().getVariant(vid);
        Global.getSector().getPlayerFleet().getFleetData().addFleetMember(vid);
        AddRemoveCommodity.addFleetMemberGainText(variant, dialog.getTextPanel());
        //done!
        return true;
    }
}