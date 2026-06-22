package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
public class BattlecarrierLogistics extends BaseHullMod {
    public static float ALL_FIGHTER_COST_PERCENT = -40.0F;
    public static float ALL_BOMBER_AND_SUPPORT_COST_PERCENT = 80.0F;

    public static float REPLACEMENT_TIME_PERCENT = -20.0F;


    public BattlecarrierLogistics() {
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDynamic().getMod(Stats.FIGHTER_COST_MOD).modifyPercent(id, ALL_FIGHTER_COST_PERCENT);
        stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyPercent(id, ALL_BOMBER_AND_SUPPORT_COST_PERCENT);
        stats.getDynamic().getMod(Stats.SUPPORT_COST_MOD).modifyPercent(id, ALL_BOMBER_AND_SUPPORT_COST_PERCENT);
        stats.getDynamic().getMod(Stats.FIGHTER_REARM_TIME_MULT).modifyPercent(id, REPLACEMENT_TIME_PERCENT);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {

        if (index == 0) {
            return "" + REPLACEMENT_TIME_PERCENT + "%";
        } else if (index == 1) {
            return "" + ALL_FIGHTER_COST_PERCENT +"%";
        } else
            return "" + ALL_BOMBER_AND_SUPPORT_COST_PERCENT + "%";
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }
}
