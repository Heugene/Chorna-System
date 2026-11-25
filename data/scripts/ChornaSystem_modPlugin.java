package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.ids.*;

//for planets
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;

//for system generation
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Random;
import java.util.List;

public class ChornaSystem_modPlugin extends BaseModPlugin {

    public static SectorAPI sector;
    private static StarSystemAPI system;

    public static boolean startWithAColony = true;

    public static PersonAPI corporatePrincess;

    @Override
    public void onNewGame() {
        sector = Global.getSector();
        ChornaStarSystemCreator.SetSector(sector);

        system = sector.getStarSystem("Chorna");

        ChornaStarSystemCreator.SetStartWithAColony(startWithAColony);
        sector.getMemoryWithoutUpdate().set("$chrn_StarforgeReclaimed", startWithAColony);

        SetPlayerFaction();
    }

    @Override
    public void onNewGameAfterProcGen()
    {
        ChornaStarSystemCreator.GenerateChornaSystem();
    }

    public void onNewGameAfterEconomyLoad() {
        if (startWithAColony) {
            CreateCatherine();
        }
        SaveChornaStarForgeStationCommanderId();
    }

    /**
     * Called when the player loads a saved game.
     *
     * @param newGame true if the save game was just created for the first time.
     *                Note that there are a few `onGameLoad` methods that may be a better choice than using this parameter
     */
    @Override
    public void onGameLoad(boolean newGame) {
        super.onGameLoad(newGame);
        if (newGame && startWithAColony) {
            SetRelations();
        }
    }

    private void SetRelations() {
        FactionAPI playerFaction = Global.getSector().getPlayerFaction();
        playerFaction.setRelationship(Factions.HEGEMONY, RepLevel.INHOSPITABLE);
        playerFaction.setRelationship(Factions.DIKTAT, RepLevel.SUSPICIOUS);
        playerFaction.setRelationship(Factions.INDEPENDENT, RepLevel.FRIENDLY);
        playerFaction.setRelationship(Factions.LUDDIC_CHURCH, RepLevel.HOSTILE);
        playerFaction.setRelationship(Factions.LUDDIC_PATH, RepLevel.HOSTILE);
        playerFaction.setRelationship(Factions.TRITACHYON, RepLevel.FAVORABLE);
        playerFaction.setRelationship(Factions.PERSEAN, RepLevel.SUSPICIOUS);
    }

    private void CreateCatherine() {
        ImportantPeopleAPI importantPeople = Global.getSector().getImportantPeople();

        MarketAPI market = Global.getSector().getEconomy().getMarket("chorna_starforge_market");
        if (market != null) {
            corporatePrincess = Global.getFactory().createPerson();
            corporatePrincess.setId("chrn_Catherine");
            corporatePrincess.setFaction("tritachyon");
            corporatePrincess.setGender(FullName.Gender.FEMALE);
            corporatePrincess.setPostId(Ranks.POST_SPECIAL_AGENT);
            corporatePrincess.setRankId(Ranks.SPACE_COMMANDER);
            corporatePrincess.setImportance(PersonImportance.HIGH);
            corporatePrincess.getName().setFirst("Catherine");
            corporatePrincess.getName().setLast("Knight");
            corporatePrincess.setPersonality(Personalities.STEADY);
            corporatePrincess.setPortraitSprite("graphics/portraits/catherine.png");
            corporatePrincess.addTag("military");
            corporatePrincess.addTag("trade");
            corporatePrincess.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
            corporatePrincess.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
            corporatePrincess.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
            corporatePrincess.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
            corporatePrincess.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
            corporatePrincess.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
            corporatePrincess.getStats().setLevel(6);
            importantPeople.addPerson(corporatePrincess);

            //market.getCommDirectory().addPerson(corporatePrincess, 0);
            market.addPerson(corporatePrincess);
        }
    }

    private void SetPlayerFaction() {
        //Player Faction
        FactionAPI playerFaction = sector.getPlayerFaction();
        playerFaction.setFactionLogoOverride("graphics/factions/custom/player_logo05_flag.png");
        playerFaction.setFactionCrestOverride("graphics/factions/custom/player_logo05_crest.png");
        playerFaction.setDisplayNameOverride("Chorna LLC");
        playerFaction.setShipNamePrefixOverride("CHRN");

        playerFaction.addKnownShip("hammerhead", true);
        playerFaction.addKnownShip("vigilance", true);
        playerFaction.addKnownShip("valkyrie", true);
        playerFaction.addKnownShip("gemini", true);
        playerFaction.addKnownShip("falcon", true);
        playerFaction.addKnownShip("shrike", true);
        playerFaction.addKnownShip("chrn_kramar", true);
        playerFaction.addKnownShip("chrn_obriy", true);
    }

    private void SaveChornaStarForgeStationCommanderId() {
        MarketAPI chornaStarforgeMarket = Global.getSector().getEconomy().getMarket("chorna_starforge_market");
        List<PersonAPI> people = chornaStarforgeMarket.getPeopleCopy();
        for (PersonAPI person : people) {
            if (person.getPostId().equals(Ranks.POST_STATION_COMMANDER)) {
                sector.getMemoryWithoutUpdate().set("$chrn_starforgeCommanderId", person.getId());
            }
        }
    }
}

