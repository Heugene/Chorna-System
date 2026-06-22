//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class VanometricSuperConductor extends BaseHullMod {
    public static float FLUX_DISSIPATION_FLAT = 600.0F;

    public VanometricSuperConductor() {
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFluxDissipation().modifyFlat(id, FLUX_DISSIPATION_FLAT);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return String.valueOf(FLUX_DISSIPATION_FLAT);
    }
}
