package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.StarAge;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class ChornaStarSystemCreator {

    final static Vector2f DEFAULT_CHORNA_SYSTEM_LOCATION = new Vector2f(-750, -5250);

    private static SectorAPI sector;
    private static StarSystemAPI system;
    private static PlanetAPI star;

    private static boolean startWithAColony;

    public static void SetSector(SectorAPI newSector) {
        sector = newSector;
    }

    public static void SetStartWithAColony(boolean flag){
        startWithAColony = flag;
    }

    public static void GenerateChornaSystem() {
        system = sector.createStarSystem("Chorna");
        Random r = new Random();

        Constellation constellation = Utils.getNearestConstellation(Misc.getCoreCenter());
        List<StarSystemAPI> systems = Utils.getNearbyStarSystems(constellation.getLocation(), 15f);

        Vector2f chorna_system_location = Utils.getUnoccupiedLocation(constellation.getLocation(), systems, system, 900f);

        if (chorna_system_location == null) {
            chorna_system_location = DEFAULT_CHORNA_SYSTEM_LOCATION;
            system.getLocation().set(chorna_system_location);
        }
        else {
            system.getLocation().set(chorna_system_location);
            constellation.getSystems().add(system);
            system.setConstellation(constellation);
        }
        Utils.clearHyperspaceNebulaAroundSystem(system);

        //Star
        star = system.initStar(
                "chorna",
                "star_yellow", //star types located in starsector-core\data\config\planets.json
                800,
                chorna_system_location.x,
                chorna_system_location.y,
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

        CreateChornaStarforge();
        // CreateIgneaPlanet();
        CreateTwinPlanets();
        GenerateExtraEntities();
        GenerateJumpPoints();
    }

    private static void CreateChornaStarforge() {
        String factionId = startWithAColony ? "player" : Factions.INDEPENDENT;

        SectorEntityToken chorna_starforge = system.addCustomEntity("chorna_starforge", "Chorna Starforge", "station_hightech3", factionId);
        chorna_starforge.setCircularOrbitPointingDown(system.getEntityById("chorna"), 270, 3720, 312);
        chorna_starforge.setInteractionImage("illustrations", "urban02");

        MarketAPI chorna_starforge_market = Global.getFactory().createMarket("chorna_starforge_market", chorna_starforge.getName(), 5);
        chorna_starforge_market.setPrimaryEntity(chorna_starforge);
        chorna_starforge.setMarket(chorna_starforge_market);

        chorna_starforge_market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        chorna_starforge_market.getTariff().modifyFlat("generator", 0.25f);

        chorna_starforge_market.setPlanetConditionMarketOnly(false);
        chorna_starforge_market.addCondition(Conditions.HABITABLE);
        chorna_starforge_market.addCondition(Conditions.NO_ATMOSPHERE);
        chorna_starforge_market.addCondition(Conditions.VERY_HOT);
        chorna_starforge_market.addCondition(Conditions.OUTPOST);
        chorna_starforge_market.addCondition(Conditions.POPULATION_5);

        chorna_starforge_market.setFactionId(factionId);
        chorna_starforge_market.setPlayerOwned(startWithAColony);

        chorna_starforge_market.addIndustry(Industries.POPULATION);
        chorna_starforge_market.addIndustry(Industries.SPACEPORT);
        chorna_starforge_market.addIndustry(Industries.STARFORTRESS_HIGH);
        chorna_starforge_market.addIndustry(Industries.MILITARYBASE);
        chorna_starforge_market.addIndustry(Industries.HEAVYINDUSTRY);
        chorna_starforge_market.addIndustry(Industries.REFINING);

        chorna_starforge_market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        chorna_starforge_market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        chorna_starforge_market.addSubmarket(Submarkets.SUBMARKET_OPEN);

        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        globalEconomy.addMarket(chorna_starforge_market, false);

        chorna_starforge_market.addImmigrationModifier((market, incoming) -> {
            incoming.add("size maxed", 0);
            incoming.getWeight().modifyMult("size maxed", 0, "The Chorna Starforge station cannot sustain more people.");
        });

    }

    private static void CreateTwinPlanets() {
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
        yaroslava_mirror3.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 180, 400, 366);
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
        yaroslava_shade1.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), -26, 390, 366);
        yaroslava_shade2.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 0, 425, 366);
        yaroslava_shade3.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 26, 390, 366);
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

    private static void GenerateExtraEntities() {
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

    private static void GenerateJumpPoints() {
        system.autogenerateHyperspaceJumpPoints(false, true);
        //gas giant = false, fringe = false / generates star gravity well
    }

}
