package com.fs.starfarer.api.impl.campaign.rulecmd;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.shared.PlayerTradeDataForSubmarket;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
public class ShowSkills extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId,
                           InteractionDialogAPI dialog,
                           List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) return false;
        SectorEntityToken target = (SectorEntityToken) dialog.getInteractionTarget();
        String officerid = params.get(0).getString(memoryMap);
        PersonAPI officer = Global.getSector().getImportantPeople().getPerson(officerid);

        if (officer != null) {
            MutableCharacterStatsAPI stats = officer.getStats();
            TextPanelAPI text = dialog.getTextPanel();
            Color hl = Misc.getHighlightColor();
            Color red = Misc.getNegativeHighlightColor();
            text.addSkillPanel(officer, false);
            text.setFontSmallInsignia();
            String personality = Misc.lcFirst(officer.getPersonalityAPI().getDisplayName());
            text.addParagraph("Personality: " + personality + ", level: " + stats.getLevel());
            text.highlightInLastPara(hl, new String[]{personality, "" + stats.getLevel()});
            text.addParagraph(officer.getPersonalityAPI().getDescription());
            text.setFontInsignia();
            //done!
            return true;
        }
        else
        {
            Global.getSector().getPlayerFleet().getFleetData().addOfficer(Global.getSector().getImportantPeople().getPerson("chrn_Catherine"));
        }
        return false;
    }
}