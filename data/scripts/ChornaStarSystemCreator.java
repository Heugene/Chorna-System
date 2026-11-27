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
                PickFrom(StarTypes.YELLOW, StarTypes.ORANGE), //star types located in starsector-core\data\config\planets.json
                800,
                chorna_system_location.x,
                chorna_system_location.y,
                600);  //id, type, radius, x coordinate, y coordinate, corona radius

        system.setBackgroundTextureFilename("graphics/mod/backgrounds/modbg.jpg");
        system.setLightColor(new Color(255, 220, 190)); // light color in entire system, affects all entities
    }

    private static String PickFrom(String ... options) {
        Random rand = new Random();
        return options[rand.nextInt(options.length)];
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

        SetStarforgeConditions(chorna_starforge_market);

        chorna_starforge_market.setFactionId(factionId);
        chorna_starforge_market.setPlayerOwned(startWithAColony);

        SetStarforgeIndustries(chorna_starforge_market);

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

    private static void SetStarforgeIndustries(MarketAPI chorna_starforge_market) {
        chorna_starforge_market.addIndustry(Industries.POPULATION);
        chorna_starforge_market.addIndustry(Industries.SPACEPORT);
        chorna_starforge_market.addIndustry(Industries.STARFORTRESS_HIGH);
        chorna_starforge_market.addIndustry(Industries.MILITARYBASE);
        chorna_starforge_market.addIndustry(Industries.HEAVYINDUSTRY);
        chorna_starforge_market.addIndustry(Industries.REFINING);
    }

    private static void SetStarforgeConditions(MarketAPI chorna_starforge_market) {
        chorna_starforge_market.setPlanetConditionMarketOnly(false);
        chorna_starforge_market.addCondition(Conditions.HABITABLE);
        chorna_starforge_market.addCondition(Conditions.NO_ATMOSPHERE);
        chorna_starforge_market.addCondition(Conditions.VERY_HOT);
        chorna_starforge_market.addCondition(Conditions.OUTPOST);
        chorna_starforge_market.addCondition(Conditions.POPULATION_5);
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

        SetIskemarConditions(iskemar);
    }

    private static void SetIskemarConditions(PlanetAPI iskemar) {
        MarketAPI iskemar_market = Global.getFactory().createMarket(chrnIds.iskemarMarket, iskemar.getName(), 0);
        iskemar_market.setPlanetConditionMarketOnly(true);
        iskemar_market.addCondition(Conditions.WATER_SURFACE);
        iskemar_market.addCondition(Conditions.HABITABLE);
        iskemar_market.addCondition(Conditions.TECTONIC_ACTIVITY);
        iskemar_market.addCondition(PickFrom(
                Conditions.MILD_CLIMATE,
                Conditions.EXTREME_WEATHER,
                Conditions.POOR_LIGHT
        ));
        iskemar_market.addCondition(PickFrom(
                Conditions.ORE_MODERATE,
                Conditions.ORE_ABUNDANT,
                Conditions.ORE_RICH));
        iskemar_market.addCondition(PickFrom(
                Conditions.VOLATILES_TRACE,
                Conditions.VOLATILES_DIFFUSE,
                Conditions.VOLATILES_ABUNDANT
        ));
        iskemar_market.addCondition(Conditions.RARE_ORE_ABUNDANT);
        iskemar_market.addCondition(PickFrom(
                Conditions.ORGANICS_COMMON,
                Conditions.ORGANICS_ABUNDANT,
                Conditions.ORGANICS_PLENTIFUL
        ));
        iskemar_market.setPrimaryEntity(iskemar);
        iskemar.setMarket(iskemar_market);
    }

    private static void CreateYaroslavaPlanet(SectorEntityToken chorna_gate) {
        PlanetAPI yaroslava;
        yaroslava = system.addPlanet(chrnIds.yaroslavaId, chorna_gate, "Yaroslava", "jungle", 0, 180, 800, 90);
        //id, focus, name, type (starsector-core\data\config\planets.json), angle, radius, orbit radius, orbit days
        yaroslava.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "mod_yaroslava")); //graphics\mod\planets\mod_yaroslava.jpg
        yaroslava.applySpecChanges();

        SpawnStellarEntities(yaroslava, StellarEntityType.Mirror, new String[]{ "Alpha", "Beta", "Gamma", "Delta", "Epsilon" }, 120, 240, 400, 366 );
        SpawnStellarEntities(yaroslava, StellarEntityType.Shade, new String[]{ "Zeta", "Eta", "Theta" }, -26, 26, 400, 366);

        SetYaroslavaConditions(yaroslava);
    }

    private static void SetYaroslavaConditions(PlanetAPI yaroslava) {
        MarketAPI yaroslava_market = Global.getFactory().createMarket(chrnIds.yaroslavaMarketId, yaroslava.getName(), 0);
        yaroslava_market.setPlanetConditionMarketOnly(true);
        yaroslava_market.addCondition(Conditions.HABITABLE);
        yaroslava_market.addCondition(Conditions.HOT);
        yaroslava_market.addCondition(Conditions.TECTONIC_ACTIVITY);
        yaroslava_market.addCondition(PickFrom(
                Conditions.FARMLAND_ADEQUATE,
                Conditions.FARMLAND_RICH,
                Conditions.FARMLAND_BOUNTIFUL
                ));  //planet conditions located in: starsector-core\data\campaign\market_conditions.csv
        yaroslava_market.addCondition(PickFrom(
                Conditions.ORE_MODERATE,
                Conditions.ORE_ABUNDANT,
                Conditions.ORE_RICH
        ));
        yaroslava_market.addCondition(Conditions.RUINS_VAST);
        yaroslava_market.addCondition(PickFrom(
                Conditions.ORGANICS_ABUNDANT,
                Conditions.ORGANICS_PLENTIFUL
        ));
        yaroslava_market.addCondition(PickFrom(
                Conditions.MILD_CLIMATE,
                Conditions.RARE_ORE_SPARSE,
                Conditions.RARE_ORE_MODERATE,
                Conditions.LOW_GRAVITY,
                Conditions.INIMICAL_BIOSPHERE

        ));
        yaroslava_market.addCondition(Conditions.POOR_LIGHT);
        yaroslava_market.addCondition(Conditions.SOLAR_ARRAY);
        yaroslava_market.setPrimaryEntity(yaroslava);
        yaroslava.setMarket(yaroslava_market);
    }

    private static void SpawnStellarEntities(SectorEntityToken orbitCenter, StellarEntityType entityType, String[] idNames, float angleMin, float angleMax, float orbitRadius, float orbitDays) {
        SectorEntityToken newEntity;

        String _entityType = Entities.STELLAR_MIRROR;
        switch (entityType) {
            case Shade: {
                _entityType = Entities.STELLAR_SHADE;
            }
            break;
            case Mirror: {
                _entityType = Entities.STELLAR_MIRROR;
            }
        }
        String _entityTypeShort = _entityType.split("_")[1];
        String _entityTypeShortCapitalLetter = _entityTypeShort.substring(0, 1).toUpperCase() + _entityTypeShort.substring(1);

        String newEntityId = orbitCenter.getName().toLowerCase() + "_" + _entityTypeShort;
        String newEntityName = orbitCenter.getName() + " Stellar " + _entityTypeShortCapitalLetter + " ";

        float sectorAngle = Math.abs(angleMax - angleMin);
        float angleStep = sectorAngle / idNames.length;
        float currentAngle = angleMin;
        for (int i = 0; i < idNames.length; i++){
            newEntity = system.addCustomEntity(newEntityId + (i + 1), newEntityName + idNames[i], _entityType, "neutral");
            newEntity.setCircularOrbitPointingDown(orbitCenter, currentAngle, orbitRadius, orbitDays);
            newEntity.setCustomDescriptionId(_entityType);
            currentAngle += angleStep;
        }
    }

    enum StellarEntityType {
        Mirror,
        Shade
    }

    private static void GenerateExtraEntities() {
        Random rand = new Random();
        if (rand.nextInt(100) < 19) {
            SpawnCoronalHypershunt();
        }
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
                "Anomalous Coronal Hypershunt",
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
