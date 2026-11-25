package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketImmigrationModifier;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.population.PopulationComposition;
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
        system = sector.createStarSystem(chrnIds.chornaSystemName);
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

        CreateChornaStar(chorna_system_location);
        CreateAsteroidBelts();
        CreateChornaStarforge();
        CreateChornaGate();
        CreateTwinPlanets();
        GenerateExtraEntities();
        GenerateJumpPoints();
    }

    private static void CreateAsteroidBelts() {
        // Inner Ring
        system.addAsteroidBelt(star, 100, 3700, 500, 100, 190, Terrain.ASTEROID_BELT, "Omega Ring");
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 3700, 201f, null, null);
        system.addRingBand(star, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 3800, 225f, null, null);

        // Outer Ring
        system.addAsteroidBelt(star, 100, 8000, 500, 100, 190, Terrain.ASTEROID_BELT, "Outer Ring");
        system.addRingBand(star, "misc", "rings_ice0", 256f, 0, Color.white, 1300f, 8100, 225f, null, null);
    }

    private static void CreateChornaStar(Vector2f chorna_system_location) {
        star = system.initStar(
                chrnIds.chornaSystemStarId,
                StarTypes.YELLOW, //star types located in starsector-core\data\config\planets.json
                800,
                chorna_system_location.x,
                chorna_system_location.y,
                600);  //id, type, radius, x coordinate, y coordinate, corona radius

        system.setBackgroundTextureFilename("graphics/mod/backgrounds/modbg.jpg");
        system.setLightColor(new Color(255, 220, 190)); // light color in entire system, affects all entities
    }

    private static void CreateChornaStarforge() {
        String factionId = startWithAColony ? Factions.PLAYER : Factions.INDEPENDENT;

        SectorEntityToken chorna_starforge = system.addCustomEntity(chrnIds.chornaStarforgeId, "Chorna Starforge","station_hightech3", factionId);
        chorna_starforge.setCircularOrbitPointingDown(system.getEntityById(chrnIds.chornaSystemStarId), 270, 3720, 312);
        chorna_starforge.setInteractionImage("illustrations", "urban02");

        MarketAPI chorna_starforge_market = Global.getFactory().createMarket(chrnIds.chornaStarforgeMarketId, chorna_starforge.getName(), 5);
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

        chorna_starforge_market.addImmigrationModifier(new MarketImmigrationModifier() {
            @Override
            public void modifyIncoming(MarketAPI market, PopulationComposition incoming) {

                incoming.add("size maxed", 0);
                incoming.getWeight().modifyMult("size maxed", 0, "The Chorna Starforge station cannot sustain more people.");
            }
        });
    }

    private static void CreateChornaGate() {
        SectorEntityToken chorna_gate = system.addCustomEntity(
                chrnIds.gateId,
                "Chorna Gate",
                Entities.INACTIVE_GATE,
                null);
        chorna_gate.setCircularOrbit(star, 180, 6000, 366); //focus, angle, orbit radius, orbit days
    }

    private static void CreateTwinPlanets() {
        SectorEntityToken chorna_gate = system.getEntityById(chrnIds.gateId);
        CreateYaroslavaPlanet(chorna_gate);
        CreateIskemarPlanet(chorna_gate);
    }

    private static void CreateIskemarPlanet(SectorEntityToken chorna_gate) {
        PlanetAPI iskemar;
        iskemar = system.addPlanet(chrnIds.iskemarId, chorna_gate, "Iskemar", Planets.PLANET_WATER, 180, 180, 800, 90);
        //id, focus, name, type (starsector-core\data\config\planets.json), angle, radius, orbit radius, orbit days
        iskemar.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "mod_iskemar")); //graphics\mod\planets\mod_iskemar.jpg
        iskemar.applySpecChanges();

        // Iskemar Conditions
        MarketAPI iskemar_market = Global.getFactory().createMarket(chrnIds.iskemarMarket, iskemar.getName(), 0);
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

    private static void CreateYaroslavaPlanet(SectorEntityToken chorna_gate) {
        PlanetAPI yaroslava;
        yaroslava = system.addPlanet(chrnIds.yaroslavaId, chorna_gate, "Yaroslava", "jungle", 0, 180, 800, 90);
        //id, focus, name, type (starsector-core\data\config\planets.json), angle, radius, orbit radius, orbit days
        yaroslava.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "mod_yaroslava")); //graphics\mod\planets\mod_yaroslava.jpg
        yaroslava.applySpecChanges();

        // Yaroslava mirror system
        SectorEntityToken yaroslava_mirror1 = system.addCustomEntity("yaroslava_mirror1", "Yaroslava Stellar Mirror Alpha", Entities.STELLAR_MIRROR, "neutral");
        SectorEntityToken yaroslava_mirror2 = system.addCustomEntity("yaroslava_mirror2", "Yaroslava Stellar Mirror Beta", Entities.STELLAR_MIRROR, "neutral");
        SectorEntityToken yaroslava_mirror3 = system.addCustomEntity("yaroslava_mirror3", "Yaroslava Stellar Mirror Gamma", Entities.STELLAR_MIRROR, "neutral");
        SectorEntityToken yaroslava_mirror4 = system.addCustomEntity("yaroslava_mirror4", "Yaroslava Stellar Mirror Delta", Entities.STELLAR_MIRROR, "neutral");
        SectorEntityToken yaroslava_mirror5 = system.addCustomEntity("yaroslava_mirror5", "Yaroslava Stellar Mirror Epsilon", Entities.STELLAR_MIRROR, "neutral");
        yaroslava_mirror1.setCircularOrbitPointingDown(system.getEntityById(chrnIds.yaroslavaId), 180 - 60, 400, 366);
        yaroslava_mirror2.setCircularOrbitPointingDown(system.getEntityById(chrnIds.yaroslavaId), 180 - 30, 400, 366);
        yaroslava_mirror3.setCircularOrbitPointingDown(system.getEntityById(chrnIds.yaroslavaId), 180, 400, 366);
        yaroslava_mirror4.setCircularOrbitPointingDown(system.getEntityById(chrnIds.yaroslavaId), 180 + 30, 400, 366);
        yaroslava_mirror5.setCircularOrbitPointingDown(system.getEntityById(chrnIds.yaroslavaId), 180 + 60, 400, 366);
        yaroslava_mirror1.setCustomDescriptionId(Entities.STELLAR_MIRROR);
        yaroslava_mirror2.setCustomDescriptionId(Entities.STELLAR_MIRROR);
        yaroslava_mirror3.setCustomDescriptionId(Entities.STELLAR_MIRROR);
        yaroslava_mirror4.setCustomDescriptionId(Entities.STELLAR_MIRROR);
        yaroslava_mirror5.setCustomDescriptionId(Entities.STELLAR_MIRROR);

        // Yaroslava shade system
        SectorEntityToken yaroslava_shade1 = system.addCustomEntity("yaroslava_shade1", "Yaroslava Stellar Shade Zeta", Entities.STELLAR_SHADE, "neutral");
        SectorEntityToken yaroslava_shade2 = system.addCustomEntity("yaroslava_shade2", "Yaroslava Stellar Shade Eta", Entities.STELLAR_SHADE, "neutral");
        SectorEntityToken yaroslava_shade3 = system.addCustomEntity("yaroslava_shade3", "Yaroslava Stellar Shade Theta", Entities.STELLAR_SHADE, "neutral");
        yaroslava_shade1.setCircularOrbitPointingDown(system.getEntityById(chrnIds.yaroslavaId), -26, 390, 366);
        yaroslava_shade2.setCircularOrbitPointingDown(system.getEntityById(chrnIds.yaroslavaId), 0, 425, 366);
        yaroslava_shade3.setCircularOrbitPointingDown(system.getEntityById(chrnIds.yaroslavaId), 26, 390, 366);
        yaroslava_shade1.setCustomDescriptionId(Entities.STELLAR_SHADE);
        yaroslava_shade2.setCustomDescriptionId(Entities.STELLAR_SHADE);
        yaroslava_shade3.setCustomDescriptionId(Entities.STELLAR_SHADE);

        // Yaroslava Conditions
        MarketAPI yaroslava_market = Global.getFactory().createMarket(chrnIds.yaroslavaMarketId, yaroslava.getName(), 0);
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
    }

    private static void GenerateExtraEntities() {
        SpawnCoronalHypershunt();

        Random rand = new Random();
        if(rand.nextInt(100) < 49) {
            SpawnNavBuoy();
        }
        else if (rand.nextInt(100) < 49) {
            SpawnStableLocation(star, 60, 3000, 245);
        }

        if(rand.nextInt(100) < 49) {
            SpawnSensorArray();
        }
        else if (rand.nextInt(100) < 49) {
            SpawnStableLocation(star, 300, 4100, 325);
        }

        if(rand.nextInt(100) < 74) {
            SpawnCommRelay();
        }
        else {
            SpawnStableLocation(system.getEntityById(chrnIds.iskemarId), 180, 500, 53);
        }

        if(rand.nextInt(100) < 74) {
            SpawnResearchStation();
        }
        if(rand.nextInt(100) < 24) {
            SpawnMiningStation();
        }

        if(rand.nextInt(100) < 49) {
            SpawnEquipmentCache();
        }

        if(rand.nextInt(100) < 19) {
            SpawnAlphaCache();
        }

        float radiusAfter = StarSystemGenerator.addOrbitingEntities(system, star, StarAge.OLD,
                0, 1, // min/max entities to add
                9000, // radius to start adding at
                3, // name offset - next planet will be <system name> <roman numeral of this parameter + 1>
                true, // whether to use custom or system-name based names
                false); // whether to allow habitable worlds
    }

    private static void SpawnCoronalHypershunt() {
        SectorEntityToken coronalTap = system.addCustomEntity(
                chrnIds.hypershuntId,
                "Coronal Hypershunt",
                Entities.CORONAL_TAP,  //custom entity types located in: starsector-core\data\config\custom_entities.json
                "neutral");
        coronalTap.setCircularOrbitPointingDown(star, 180, 1200, 500); //focus, angle, orbit radius, orbit days
    }

    private static void SpawnStableLocation(SectorEntityToken orbitCenter, float angle, float radius, float orbitDays) {
        SectorEntityToken stableLocation = system.addCustomEntity(null, null, Entities.STABLE_LOCATION, "neutral");
        stableLocation.setCircularOrbitPointingDown(orbitCenter, angle, radius, orbitDays);
    }

    private static void SpawnAlphaCache() {
        SectorEntityToken alphaCache = system.addCustomEntity(chrnIds.alphaCacheId,
                "Heavily Shielded Cache",
                Entities.ALPHA_SITE_WEAPONS_CACHE,  //custom entity types located in: starsector-core\data\config\custom_entities.json
                "neutral");
        alphaCache.setCircularOrbitPointingDown(star, 265, 3720, 312); //focus, angle, orbit radius, orbit days
    }

    private static void SpawnEquipmentCache() {
        SectorEntityToken equipmentCache = system.addCustomEntity(chrnIds.equipmentCacheId,
                "Equipment Cache",
                Entities.EQUIPMENT_CACHE,  //custom entity types located in: starsector-core\data\config\custom_entities.json
                "neutral");
        equipmentCache.setCircularOrbitPointingDown(star, 60, 8000, 15); //focus, angle, orbit radius, orbit days
    }

    private static void SpawnMiningStation() {
        SectorEntityToken miningStation = system.addCustomEntity(chrnIds.miningStationId,
                "Mining Station",
                Entities.STATION_MINING,  //custom entity types located in: starsector-core\data\config\custom_entities.json
                "neutral");
        miningStation.setCircularOrbitPointingDown(star, 160, 8000, 312); //focus, angle, orbit radius, orbit days
    }

    private static void SpawnResearchStation() {
        SectorEntityToken researchStation = system.addCustomEntity(chrnIds.researchStationId,
                "Research Station",
                Entities.STATION_RESEARCH,  //custom entity types located in: starsector-core\data\config\custom_entities.json
                "neutral");
        researchStation.setCircularOrbitPointingDown(star, 90, 1300, 500); //focus, angle, orbit radius, orbit days
    }

    private static void SpawnCommRelay() {
        SectorEntityToken relay = system.addCustomEntity(chrnIds.relayId,
                "Chorna Relay",
                Entities.COMM_RELAY,
                "neutral");
        relay.setCircularOrbitPointingDown(system.getEntityById("iskemar"), 180, 500, 53); //focus, angle, orbit radius, orbit days
    }

    private static void SpawnSensorArray() {
        SectorEntityToken array = system.addCustomEntity(chrnIds.arrayId,
                "Chorna Array",
                Entities.SENSOR_ARRAY,
                "neutral");
        array.setCircularOrbitPointingDown(star, 300, 4100, 325); //focus, angle, orbit radius, orbit days
    }

    private static void SpawnNavBuoy() {
        SectorEntityToken buoy = system.addCustomEntity(chrnIds.buoyId,
                "Chorna Buoy",
                Entities.NAV_BUOY,
                "neutral");
        buoy.setCircularOrbitPointingDown(star, 60, 3000, 245); //focus, angle, orbit radius, orbit days
    }

    private static void GenerateJumpPoints() {
        system.autogenerateHyperspaceJumpPoints(false, true);
        //gas giant = false, fringe = false / generates star gravity well
    }

}
