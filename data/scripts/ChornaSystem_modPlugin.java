package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.*;

//for planets
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.loading.ContactTagSpec;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ChornaSystem_modPlugin extends BaseModPlugin {

    public static PersonAPI corporatePrincess;
    public static MarketAPI omega_market;
    private static SectorAPI sector;
    private static FactionAPI playerFaction;
    private static StarSystemAPI system;
    private static PlanetAPI star;

    enum SystemVariant {
        StandardFull,
        StandardSemiRandom,
        TwinPlanetFull,
        TwinPlanetSemiRandom
    }

    private static SystemVariant systemVariant;

    @Override
    public void onNewGame() {

        sector = Global.getSector();
        GenerateChornaSystem();
        SetPlayerFaction();
        GenerateExtraEntities();

        system.autogenerateHyperspaceJumpPoints(false, true); //gas giant = false, fringe = false / generates star gravity well

    }

    public void onNewGameAfterEconomyLoad() {
        CreateCatherine();
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
        if (newGame) {
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

        MarketAPI market = Global.getSector().getEconomy().getMarket("omega_market");
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
        playerFaction = sector.getPlayerFaction();
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
    }

    private void GenerateChornaSystem() {
        system = sector.createStarSystem("Chorna");

        //Star
        star = system.initStar(
                "chorna",
                "star_yellow", //star types located in starsector-core\data\config\planets.json
                800,
                -750,
                -5250,
                600);  //id, type, radius, x coordinate, y coordinate, corona radius

        system.setBackgroundTextureFilename("graphics/mod/backgrounds/modbg.jpg");
        system.setLightColor(new Color(255, 220, 190)); // light color in entire system, affects all entities

        //Coronal Hypershunt
        SectorEntityToken ccoronaltap = system.addCustomEntity("chorna_coronal_hypershunt",
                "Coronal Hypershunt",
                "coronal_tap",  //custom entity types located in: starsector-core\data\config\custom_entities.json
                "neutral");
        ccoronaltap.setCircularOrbitPointingDown(star, 180, 1200, 500); //focus, angle, orbit radius, orbit days

        // Omega Ring
        system.addAsteroidBelt(star, 100, 3700, 500, 100, 190, Terrain.ASTEROID_BELT, "Omega Ring");
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 3700, 201f, null, null);
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 3800, 225f, null, null);


        // Outer Ring
        system.addAsteroidBelt(star, 100, 8000, 500, 100, 190, Terrain.ASTEROID_BELT, "Outer Ring");
        system.addRingBand(star, "misc", "rings_ice0", 256f, 0, Color.white, 1300f, 8100, 225f, null, null);

        CreateOmegaSite();
        CreateIgneaPlanet();
        CreateTwinPlanets();

    }

    private void CreateOmegaSite() {

        // Omega site
        SectorEntityToken omega_site = system.addCustomEntity("omega_site", "Omega site", "station_hightech3", "player");
        omega_site.setCircularOrbitPointingDown(system.getEntityById("chorna"), 270, 3720, 312);
        omega_site.setInteractionImage("illustrations", "urban02");

        omega_market = Global.getFactory().createMarket("omega_market", omega_site.getName(), 4);
        omega_market.setPrimaryEntity(omega_site);
        omega_site.setMarket(omega_market);

        omega_market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        omega_market.getTariff().modifyFlat("generator", 0.25f);

        omega_market.setPlanetConditionMarketOnly(false);
        omega_market.addCondition(Conditions.HABITABLE);
        omega_market.addCondition(Conditions.NO_ATMOSPHERE);
        omega_market.addCondition(Conditions.OUTPOST);
        omega_market.addCondition(Conditions.POPULATION_4);

        omega_market.setFactionId(Factions.PLAYER);

        omega_market.addIndustry(Industries.POPULATION);
        omega_market.addIndustry(Industries.SPACEPORT);
        omega_market.addIndustry(Industries.STARFORTRESS_HIGH);
        omega_market.addIndustry(Industries.MILITARYBASE);
        omega_market.addIndustry(Industries.HEAVYINDUSTRY);
        omega_market.addIndustry(Industries.REFINING);

        omega_market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        omega_market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        omega_market.addSubmarket(Submarkets.SUBMARKET_OPEN);

        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(omega_market, false);

        omega_market.setPlayerOwned(true);
    }

    private void CreateIgneaPlanet() {
        // Ignea
        PlanetAPI ignea;

        ignea = system.addPlanet("ignea", star, "Ignea", "lava", 180, 150, 3000, 241);  //id, focus, name, type (starsector-core\data\config\planets.json), angle, radius, orbit radius, orbit days
        ignea.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "mod_ignea")); //graphics\mod\planets\mod_ignea.jpg
        ignea.applySpecChanges();


        // Ignea Conditions
        MarketAPI ignea_market = Global.getFactory().createMarket("ignea_market", ignea.getName(), 0);
        ignea_market.setPlanetConditionMarketOnly(true);
        ignea_market.addCondition(Conditions.NO_ATMOSPHERE);
        ignea_market.addCondition(Conditions.ORE_ULTRARICH);
        ignea_market.addCondition(Conditions.RARE_ORE_ULTRARICH);  //planet conditions located in: starsector-core\data\campaign\market_conditions.csv
        ignea_market.addCondition(Conditions.VERY_HOT);
        ignea_market.setPrimaryEntity(ignea);
        ignea.setMarket(ignea_market);

    }

    private void CreateTwinPlanets() {
        //Gate
        SectorEntityToken chorna_gate = system.addCustomEntity("chorna_gate",
                "Chorna Gate",
                "inactive_gate",
                null);
        chorna_gate.setCircularOrbit(star, 180, 6000, 366); //focus, angle, orbit radius, orbit days

        // Yaroslava planet
        PlanetAPI yaroslava;
        yaroslava = system.addPlanet("yaroslava", chorna_gate, "Yaroslava", "jungle", 0, 180, 800, 90);  //id, focus, name, type (starsector-core\data\config\planets.json), angle, radius, orbit radius, orbit days
        yaroslava.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "mod_yaroslava")); //graphics\mod\planets\mod_yaroslava.jpg
        yaroslava.applySpecChanges();

        // Yaroslava mirror system
        SectorEntityToken yaroslava_mirror1 = system.addCustomEntity("yaroslava_mirror1", "Yaroslava Stellar Mirror Alpha", "stellar_mirror", "neutral");
        SectorEntityToken yaroslava_mirror2 = system.addCustomEntity("yaroslava_mirror2", "Yaroslava Stellar Mirror Beta", "stellar_mirror", "neutral");
        SectorEntityToken yaroslava_mirror3 = system.addCustomEntity("yaroslava_mirror3", "Yaroslava Stellar Mirror Gamma", "stellar_mirror", "neutral");
        SectorEntityToken yaroslava_mirror4 = system.addCustomEntity("yaroslava_mirror4", "Yaroslava Stellar Mirror Gamma", "stellar_mirror", "neutral");
        SectorEntityToken yaroslava_mirror5 = system.addCustomEntity("yaroslava_mirror5", "Yaroslava Stellar Mirror Gamma", "stellar_mirror", "neutral");
        yaroslava_mirror1.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 180 - 60, 400, 366);
        yaroslava_mirror2.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 180 - 30, 400, 366);
        yaroslava_mirror3.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 180 - 0, 400, 366);
        yaroslava_mirror4.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 180 + 30, 400, 366);
        yaroslava_mirror5.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 180 + 60, 400, 366);
        yaroslava_mirror1.setCustomDescriptionId("stellar_mirror");
        yaroslava_mirror2.setCustomDescriptionId("stellar_mirror");
        yaroslava_mirror3.setCustomDescriptionId("stellar_mirror");
        yaroslava_mirror4.setCustomDescriptionId("stellar_mirror");
        yaroslava_mirror5.setCustomDescriptionId("stellar_mirror");

        // Yaroslava shade system
        SectorEntityToken yaroslava_shade1 = system.addCustomEntity("yaroslava_mirror1", "Yaroslava Stellar Shade Delta", "stellar_shade", "neutral");
        SectorEntityToken yaroslava_shade2 = system.addCustomEntity("yaroslava_mirror3", "Yaroslava Stellar Shade Epsilon", "stellar_shade", "neutral");
        SectorEntityToken yaroslava_shade3 = system.addCustomEntity("yaroslava_mirror5", "Yaroslava Stellar Shade Omega", "stellar_shade", "neutral");
        yaroslava_shade1.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 0 - 26, 390, 366);
        yaroslava_shade2.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 0 + 0, 425, 366);
        yaroslava_shade3.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 0 + 26, 390, 366);
        yaroslava_shade1.setCustomDescriptionId("stellar_shade");
        yaroslava_shade2.setCustomDescriptionId("stellar_shade");
        yaroslava_shade3.setCustomDescriptionId("stellar_shade");

        // Yaroslava Conditions
        MarketAPI yaroslava_market = Global.getFactory().createMarket("yaroslava_market", yaroslava.getName(), 0);
        yaroslava_market.setPlanetConditionMarketOnly(true);
        yaroslava_market.addCondition(Conditions.HABITABLE);
        yaroslava_market.addCondition(Conditions.TECTONIC_ACTIVITY);
        yaroslava_market.addCondition(Conditions.HOT);
        yaroslava_market.addCondition(Conditions.MILD_CLIMATE);
        yaroslava_market.addCondition(Conditions.FARMLAND_BOUNTIFUL);  //planet conditions located in: starsector-core\data\campaign\market_conditions.csv
        yaroslava_market.addCondition(Conditions.ORE_MODERATE);
        yaroslava_market.addCondition(Conditions.RUINS_VAST);
        yaroslava_market.addCondition(Conditions.ORGANICS_PLENTIFUL);
        yaroslava_market.addCondition(Conditions.POOR_LIGHT);
        yaroslava_market.addCondition(Conditions.SOLAR_ARRAY);
        yaroslava_market.addCondition(Conditions.REGIONAL_CAPITAL);
        yaroslava_market.setPrimaryEntity(yaroslava);
        yaroslava.setMarket(yaroslava_market);

        // Yaroslava planet
        PlanetAPI iskemar;
        iskemar = system.addPlanet("iskemar", chorna_gate, "Iskemar", "water", 180, 180, 800, 90);  //id, focus, name, type (starsector-core\data\config\planets.json), angle, radius, orbit radius, orbit days
        iskemar.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "mod_iskemar")); //graphics\mod\planets\mod_iskemar.jpg
        iskemar.applySpecChanges();

        // Iskemar Conditions
        MarketAPI iskemar_market = Global.getFactory().createMarket("iskemar_market", iskemar.getName(), 0);
        iskemar_market.setPlanetConditionMarketOnly(true);
        iskemar_market.addCondition(Conditions.WATER_SURFACE);
        iskemar_market.addCondition(Conditions.HABITABLE);
        iskemar_market.addCondition(Conditions.TECTONIC_ACTIVITY);
        iskemar_market.addCondition(Conditions.MILD_CLIMATE);
        iskemar_market.addCondition(Conditions.ORE_MODERATE);
        iskemar_market.addCondition(Conditions.VOLATILES_DIFFUSE);
        iskemar_market.addCondition(Conditions.RARE_ORE_ABUNDANT);
        iskemar_market.addCondition(Conditions.ORGANICS_PLENTIFUL);
        iskemar_market.addCondition(Conditions.POOR_LIGHT);
        iskemar_market.setPrimaryEntity(iskemar);
        iskemar.setMarket(iskemar_market);

    }

    private void GenerateExtraEntities() {
        Random rand = new Random();
        if(rand.nextInt(100) < 49) {
            //Buoy
            SectorEntityToken buoy = system.addCustomEntity("chorna_buoy",
                    "Chorna Buoy",
                    "nav_buoy",
                    "neutral");
            buoy.setCircularOrbitPointingDown(star, 60, 3000, 245); //focus, angle, orbit radius, orbit days
        }
        else if (rand.nextInt(100) < 49) {
            SectorEntityToken stableLocation = system.addCustomEntity(null, null, "stable_location", "neutral");
            stableLocation.setCircularOrbitPointingDown(star, 60, 3000, 245);
        }

        if(rand.nextInt(100) < 49) {
            SectorEntityToken array = system.addCustomEntity("chorna_array",
                    "Chorna Array",
                    "sensor_array",
                    "neutral");
            array.setCircularOrbitPointingDown(star, 300, 4100, 325); //focus, angle, orbit radius, orbit days
        }
        else if (rand.nextInt(100) < 49) {
            SectorEntityToken stableLocation = system.addCustomEntity(null, null, "stable_location", "neutral");
            stableLocation.setCircularOrbitPointingDown(star, 300, 4100, 325);
        }

        if(rand.nextInt(100) < 74) {
            //Relay
            SectorEntityToken relay = system.addCustomEntity("chorna_relay",
                    "Chorna Relay",
                    "comm_relay",
                    "neutral");
            relay.setCircularOrbitPointingDown(system.getEntityById("iskemar"), 180, 500, 53); //focus, angle, orbit radius, orbit days
        }
        else {
            SectorEntityToken stableLocation = system.addCustomEntity(null, null, "stable_location", "neutral");
            stableLocation.setCircularOrbitPointingDown(system.getEntityById("iskemar"), 180, 500, 53);
        }

        if(rand.nextInt(100) < 74) {
            //Research Station 1
            SectorEntityToken cresearchstation1 = system.addCustomEntity("chorna_research_station_1",
                    "Research Station",
                    "station_research",  //custom entity types located in: starsector-core\data\config\custom_entities.json
                    "neutral");
            cresearchstation1.setCircularOrbitPointingDown(star, 90, 1300, 500); //focus, angle, orbit radius, orbit days
        }
        if(rand.nextInt(100) < 24) {
            //Mining Station
            SectorEntityToken cminingstation = system.addCustomEntity("chorna_mining_station",
                    "Mining Station",
                    "station_mining",  //custom entity types located in: starsector-core\data\config\custom_entities.json
                    "neutral");
            cminingstation.setCircularOrbitPointingDown(star, 160, 8000, 312); //focus, angle, orbit radius, orbit days
        }

        if(rand.nextInt(100) < 49) {
            //Equipment Cache
            SectorEntityToken cequipcache = system.addCustomEntity("chorna_equpment_cache",
                    "Equipment Cache",
                    "equipment_cache",  //custom entity types located in: starsector-core\data\config\custom_entities.json
                    "neutral");
            cequipcache.setCircularOrbitPointingDown(star, 60, 8000, 15); //focus, angle, orbit radius, orbit days
        }

        if(rand.nextInt(100) < 19) {
            //Alpha Site Weapons Cache
            SectorEntityToken calphacache1 = system.addCustomEntity("chorna_alpha_cache",
                    "Heavily Shielded Cache",
                    "alpha_site_weapons_cache",  //custom entity types located in: starsector-core\data\config\custom_entities.json
                    "neutral");
            calphacache1.setCircularOrbitPointingDown(star, 265, 3720, 312); //focus, angle, orbit radius, orbit days
        }


        float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.OLD,
                0, 1, // min/max entities to add
                9000, // radius to start adding at
                3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true, // whether to use custom or system-name based names
                false); // whether to allow habitable worlds
    }
    private boolean CreateDonbassPlanet(byte variant) {
        // Donbass planet
        PlanetAPI donbass;

        switch (variant) {
            case 0: {
                donbass = system.addPlanet("donbass", star, "Donbass", "barren-bombarded", 150, 160, 6600, 380);  //id, focus, name, type (starsector-core\data\config\planets.json), angle, radius, orbit radius, orbit days
                donbass.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "mod_donbass")); //graphics\mod\planets\mod_donbass.jpg
                donbass.applySpecChanges();
            }
            break;

            default: {
                return false;
            }
        }

        // Donbass Conditions
        MarketAPI donbass_market = Global.getFactory().createMarket("donbass_market", donbass.getName(), 0);
        donbass_market.setPlanetConditionMarketOnly(true);
        donbass_market.addCondition(Conditions.COLD);
        donbass_market.addCondition(Conditions.POOR_LIGHT);
        donbass_market.addCondition(Conditions.ORE_MODERATE);
        donbass_market.addCondition(Conditions.RARE_ORE_MODERATE);
        donbass_market.addCondition(Conditions.VOLATILES_PLENTIFUL);
        donbass_market.addCondition(Conditions.LOW_GRAVITY);
        donbass_market.addCondition(Conditions.THIN_ATMOSPHERE);
        donbass_market.setPrimaryEntity(donbass);
        donbass.setMarket(donbass_market);

        return true;
    }
}

