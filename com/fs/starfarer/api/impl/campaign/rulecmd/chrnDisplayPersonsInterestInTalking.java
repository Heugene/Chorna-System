package com.fs.starfarer.api.impl.campaign.rulecmd;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.List;
import java.util.Map;
public class chrnDisplayPersonsInterestInTalking extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId,
                           InteractionDialogAPI dialog,
                           List<Misc.Token> params,
                           Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) return false;

        String MarketId = params.get(0).getString(memoryMap);
        String PersonId = params.get(1).getString(memoryMap);
        Boolean flag = params.get(2).getBoolean(memoryMap);

        List<PersonAPI> peopleOfGivenMarket = Global.getSector().getEconomy().getMarket(MarketId).getPeopleCopy();

        for (PersonAPI person : peopleOfGivenMarket) {
            if (person.getId().equals(PersonId)) {
                if (flag) {
                    Misc.makeImportant(person, "chrnWhatever");
                }
                else {
                    Misc.makeUnimportant(person, "chrnWhatever");
                }

            }
        }

        return true;
        //done!
    }
}