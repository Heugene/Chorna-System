package com.fs.starfarer.api.impl.campaign.rulecmd;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
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
public class chrnSetReclaimDealCost extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId,
                           InteractionDialogAPI dialog,
                           List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) return false;

        final int baseIndependentPortion = 1_000_000;
        final int baseHegemonyPortion = 1_500_000;

        final boolean IsCommissionedByHegemony = Misc.getCommissionFactionId() != null &&
                Misc.getCommissionFactionId().equals(Factions.HEGEMONY);
        final float hegemonyRep = Global.getSector().getPlayerFaction().getRelationship(Factions.HEGEMONY);
        final float commissionDiscount = (float) (IsCommissionedByHegemony ? 0.5 : 1.0);

        int calculatedHegemonyPortion = (int) (baseHegemonyPortion * (2 - hegemonyRep) * commissionDiscount);
        int totalDealCost = baseIndependentPortion + calculatedHegemonyPortion;

        Global.getSector().getMemoryWithoutUpdate().set("$chrn_reclaimBaseIndPortion", baseIndependentPortion);
        Global.getSector().getMemoryWithoutUpdate().set("$chrn_reclaimBaseHegPortion", baseHegemonyPortion);
        Global.getSector().getMemoryWithoutUpdate().set("$chrn_reclaimCalculatedHegPortion", calculatedHegemonyPortion);
        Global.getSector().getMemoryWithoutUpdate().set("$chrn_reclaimTotalDealCost", totalDealCost);

        return true;
        //done!
    }
}
