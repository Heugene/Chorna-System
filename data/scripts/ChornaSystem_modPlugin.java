package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.impl.campaign.ids.*;

//for planets
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;

import java.awt.*;

public class ChornaSystem_modPlugin extends BaseModPlugin {
    @Override
    public void onNewGame() {

		SectorAPI sector = Global.getSector();
		StarSystemAPI system = sector.createStarSystem("Chorna");

		//Player Faction
		FactionAPI playerFaction = sector.getPlayerFaction();
		playerFaction.setFactionLogoOverride("graphics/factions/custom/player_logo05_flag.png");
		playerFaction.setFactionCrestOverride("graphics/factions/custom/player_logo05_crest.png");
		playerFaction.setDisplayNameOverride("Chorna LLC");
		playerFaction.setShipNamePrefixOverride("CHRN");

		playerFaction.addKnownShip("omen", true);
		playerFaction.addKnownShip("shrike", true);
		playerFaction.addKnownShip("aurora", true);

//		playerFaction.setRelationship(Factions.HEGEMONY, RepLevel.INHOSPITABLE);
//		playerFaction.setRelationship(Factions.DIKTAT, RepLevel.SUSPICIOUS);
//		playerFaction.setRelationship(Factions.INDEPENDENT, RepLevel.FRIENDLY);
//		playerFaction.setRelationship(Factions.LUDDIC_CHURCH, RepLevel.HOSTILE);
//		playerFaction.setRelationship(Factions.LUDDIC_PATH, RepLevel.HOSTILE);
//		playerFaction.setRelationship(Factions.TRITACHYON, RepLevel.FAVORABLE);
//		playerFaction.setRelationship(Factions.PERSEAN, RepLevel.SUSPICIOUS);


		//Star
		PlanetAPI star = system.initStar(
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

		// Ignea
		PlanetAPI ignea = system.addPlanet("ignea", star, "Ignea","lava", 180, 150, 3000, 241);  //id, focus, name, type (starsector-core\data\config\planets.json), angle, radius, orbit radius, orbit days
		ignea.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "mod_ignea")); //graphics\mod\planets\mod_ignea.jpg
		ignea.applySpecChanges();

		// Ignea Conditions
		MarketAPI ignea_market = Global.getFactory().createMarket("ignea_market", ignea.getName(), 0);
		ignea_market.setPlanetConditionMarketOnly(true);
		ignea_market.addCondition(Conditions.NO_ATMOSPHERE);
		ignea_market.addCondition(Conditions.ORE_ULTRARICH);
		ignea_market.addCondition(Conditions.RARE_ORE_ULTRARICH);  //planet conditions located in: starsector-core\data\campaign\market_conditions.csv
		ignea_market.addCondition(Conditions.RUINS_VAST);
		ignea_market.addCondition(Conditions.VERY_HOT);
		ignea_market.setPrimaryEntity(ignea);
		ignea.setMarket(ignea_market);

		//Buoy
		SectorEntityToken buoy = system.addCustomEntity("chorna_buoy",
				"Chorna Buoy",
				"nav_buoy",
				"neutral");
		buoy.setCircularOrbitPointingDown(ignea, 60, 400, 48); //focus, angle, orbit radius, orbit days

		// Omega Ring
		system.addAsteroidBelt(star, 100, 3700, 500, 100, 190, Terrain.ASTEROID_BELT, "Omega Ring");
		system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 3700, 201f, null, null);
		system.addRingBand(star, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 3800, 225f, null, null);

		// Omega site
		SectorEntityToken omega_site = system.addCustomEntity("omega_site", "Omega site", "station_hightech3", "player");
		omega_site.setCircularOrbitPointingDown(system.getEntityById("chorna"), 270, 3720, 312);
		omega_site.setInteractionImage("illustrations", "urban02");

		MarketAPI omega_market = Global.getFactory().createMarket("omega_market", omega_site.getName(), 4);
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

		//Array
		SectorEntityToken array = system.addCustomEntity("chorna_array",
				"Chorna Array",
				"sensor_array",
				"neutral");
		array.setCircularOrbitPointingDown(star, 300, 4100, 325); //focus, angle, orbit radius, orbit days

		// Yaroslava planet

		PlanetAPI yaroslava = system.addPlanet("yaroslava", star, "Yaroslava", "jungle", 0, 200, 5000, 366);  //id, focus, name, type (starsector-core\data\config\planets.json), angle, radius, orbit radius, orbit days
		yaroslava.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "mod_yaroslava")); //graphics\mod\planets\mod_yaroslava.jpg
		yaroslava.applySpecChanges();

		// Yaroslava mirror system
		SectorEntityToken yaroslava_mirror1 = system.addCustomEntity("yaroslava_mirror1", "Yaroslava Stellar Mirror Alpha", "stellar_mirror", "neutral");
		SectorEntityToken yaroslava_mirror2 = system.addCustomEntity("yaroslava_mirror2", "Yaroslava Stellar Mirror Beta", "stellar_mirror", "neutral");
		SectorEntityToken yaroslava_mirror3 = system.addCustomEntity("yaroslava_mirror3", "Yaroslava Stellar Mirror Gamma", "stellar_mirror", "neutral");
		yaroslava_mirror1.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 0 - 30, 400, 366);
		yaroslava_mirror2.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 0 - 0, 400, 366);
		yaroslava_mirror3.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 0 + 30, 400, 366);
		yaroslava_mirror1.setCustomDescriptionId("stellar_mirror");
		yaroslava_mirror2.setCustomDescriptionId("stellar_mirror");
		yaroslava_mirror3.setCustomDescriptionId("stellar_mirror");

		// Yaroslava shade system
		SectorEntityToken yaroslava_shade1 = system.addCustomEntity("yaroslava_mirror1", "Yaroslava Stellar Shade Delta", "stellar_shade", "neutral");
		SectorEntityToken yaroslava_shade2 = system.addCustomEntity("yaroslava_mirror3", "Yaroslava Stellar Shade Epsilon", "stellar_shade", "neutral");
		SectorEntityToken yaroslava_shade3 = system.addCustomEntity("yaroslava_mirror5", "Yaroslava Stellar Shade Omega", "stellar_shade", "neutral");
		yaroslava_shade1.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 180 - 26, 390, 366);
		yaroslava_shade2.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 180 + 0, 425, 366);
		yaroslava_shade3.setCircularOrbitPointingDown(system.getEntityById("yaroslava"), 180 + 26, 390, 366);
		yaroslava_shade1.setCustomDescriptionId("stellar_shade");
		yaroslava_shade2.setCustomDescriptionId("stellar_shade");
		yaroslava_shade3.setCustomDescriptionId("stellar_shade");

		// Yaroslava Conditions
		MarketAPI yaroslava_market = Global.getFactory().createMarket("yaroslava_market", yaroslava.getName(), 0);
		yaroslava_market.setPlanetConditionMarketOnly(true);
		yaroslava_market.addCondition(Conditions.HABITABLE);
		yaroslava_market.addCondition(Conditions.HOT);
		yaroslava_market.addCondition(Conditions.MILD_CLIMATE);
		yaroslava_market.addCondition(Conditions.FARMLAND_BOUNTIFUL);  //planet conditions located in: starsector-core\data\campaign\market_conditions.csv
		yaroslava_market.addCondition(Conditions.ORE_MODERATE);
		yaroslava_market.addCondition(Conditions.RUINS_VAST);
		yaroslava_market.addCondition(Conditions.ORGANICS_PLENTIFUL);
		yaroslava_market.addCondition(Conditions.POOR_LIGHT);
		yaroslava_market.addCondition(Conditions.SOLAR_ARRAY);
		yaroslava_market.addCondition("trade_center");
		yaroslava_market.addCondition(Conditions.REGIONAL_CAPITAL);
		yaroslava_market.setPrimaryEntity(yaroslava);
		yaroslava.setMarket(yaroslava_market);

		//Gate
		SectorEntityToken chorna_gate = system.addCustomEntity("chorna_gate",
				 "Chorna Gate",
				 "inactive_gate",
				 null);
		chorna_gate.setCircularOrbit(yaroslava, 180, 700, 90); //focus, angle, orbit radius, orbit days

		// Donbass planet
		PlanetAPI donbass = system.addPlanet("donbass", star, "Donbass", "barren-bombarded", 150, 160, 6600, 380);  //id, focus, name, type (starsector-core\data\config\planets.json), angle, radius, orbit radius, orbit days
		donbass.getSpec().setTexture(Global.getSettings().getSpriteName("planets", "mod_donbass")); //graphics\mod\planets\mod_donbass.jpg
		donbass.applySpecChanges();

		// Donbass Conditions
		MarketAPI donbass_market = Global.getFactory().createMarket("donbass_market", donbass.getName(), 0);
		donbass_market.setPlanetConditionMarketOnly(true);
		donbass_market.addCondition(Conditions.COLD);
		donbass_market.addCondition(Conditions.POOR_LIGHT);
		donbass_market.addCondition(Conditions.ORE_MODERATE);
		donbass_market.addCondition(Conditions.RARE_ORE_RICH);
		donbass_market.addCondition(Conditions.RUINS_VAST);
		donbass_market.addCondition(Conditions.VOLATILES_PLENTIFUL);
		donbass_market.addCondition(Conditions.ORGANICS_TRACE);
		donbass_market.addCondition(Conditions.LOW_GRAVITY);
		donbass_market.addCondition(Conditions.THIN_ATMOSPHERE);
		donbass_market.setPrimaryEntity(donbass);
		donbass.setMarket(donbass_market);

		//Relay
		SectorEntityToken relay = system.addCustomEntity("chorna_relay",
				 "Chorna Relay",
				 "comm_relay",
				 "neutral");
		relay.setCircularOrbitPointingDown(donbass, 180, 400, 53); //focus, angle, orbit radius, orbit days

		// Outer Ring
		system.addAsteroidBelt(star, 100, 7700, 500, 100, 190, Terrain.ASTEROID_BELT, "Outer Ring");
		system.addRingBand(star, "misc", "rings_asteroids0", 256f, 0, Color.white, 256f, 7700, 201f, null, null);
		system.addRingBand(star, "misc", "rings_asteroids0", 256f, 1, Color.white, 256f, 7800, 225f, null, null);

		//Cryosleeper
		SectorEntityToken ccryosleeper = system.addCustomEntity("chorna_cryosleeper",
				 "Domain-era Cryosleeper",
				 "derelict_cryosleeper",  //custom entity types located in: starsector-core\data\config\custom_entities.json
				 "neutral");
		ccryosleeper.setCircularOrbitPointingDown(star, 180, 10000, 500); //focus, angle, orbit radius, orbit days

		system.autogenerateHyperspaceJumpPoints(false,true); //gas giant = false, fringe = false / generates star gravity well


		// Generate some juicy lootable objects

		//Research Station 1
		SectorEntityToken cresearchstation1 = system.addCustomEntity("chorna_research_station_1",
				 "Research Station",
				 "station_research",  //custom entity types located in: starsector-core\data\config\custom_entities.json
				 "neutral");
		cresearchstation1.setCircularOrbitPointingDown(star, 90, 1300, 500); //focus, angle, orbit radius, orbit days

		//Research Station 2
		SectorEntityToken cresearchstation2 = system.addCustomEntity("chorna_research_station_2",
				 "Research Station",
				 "station_research",  //custom entity types located in: starsector-core\data\config\custom_entities.json
				 "neutral");
		cresearchstation2.setCircularOrbitPointingDown(star, 270, 1300, 500); //focus, angle, orbit radius, orbit days

		//Mining Station
		SectorEntityToken cminingstation = system.addCustomEntity("chorna_mining_station",
				"Mining Station",
				"station_mining",  //custom entity types located in: starsector-core\data\config\custom_entities.json
				"neutral");
		cminingstation.setCircularOrbitPointingDown(star, 160, 7720, 312); //focus, angle, orbit radius, orbit days

		//Equipment Cache
		SectorEntityToken cequipcache = system.addCustomEntity("chorna_equpment_cache",
				 "Equipment Cache",
				 "equipment_cache",  //custom entity types located in: starsector-core\data\config\custom_entities.json
				 "neutral");
		cequipcache.setCircularOrbitPointingDown(ccryosleeper, 60, 400, 15); //focus, angle, orbit radius, orbit days

		//Alpha Site Weapons Cache
		SectorEntityToken calphacache1 = system.addCustomEntity("chorna_alpha_cache",
				 "Heavily Shielded Cache",
				 "alpha_site_weapons_cache",  //custom entity types located in: starsector-core\data\config\custom_entities.json
				 "neutral");
		calphacache1.setCircularOrbitPointingDown(star, 265, 3720, 312); //focus, angle, orbit radius, orbit days

		//Alpha Site Weapons Cache
		SectorEntityToken calphacache2 = system.addCustomEntity("chorna_alpha_cache2",
				"Heavily Shielded Cache",
				"alpha_site_weapons_cache",  //custom entity types located in: starsector-core\data\config\custom_entities.json
				"neutral");
		calphacache2.setCircularOrbitPointingDown(star, 275, 3720, 312); //focus, angle, orbit radius, orbit days

    }

	public void onNewGameAfterEconomyLoad() {
		ImportantPeopleAPI importantPeople = Global.getSector().getImportantPeople();

		MarketAPI market = Global.getSector().getEconomy().getMarket("omega_market");
		if (market != null) {
			PersonAPI corporatePrincess = Global.getFactory().createPerson();
			corporatePrincess.setId("chrn_Catherine");
			corporatePrincess.setFaction("tritachyon");
			corporatePrincess.setGender(FullName.Gender.FEMALE);
			corporatePrincess.setPostId(Ranks.POST_SPECIAL_AGENT);
			corporatePrincess.setRankId(Ranks.CITIZEN);
			corporatePrincess.setImportance(PersonImportance.VERY_HIGH);
			corporatePrincess.getName().setFirst("Catherine");
			corporatePrincess.getName().setLast("Knight");
			corporatePrincess.setPersonality(Personalities.STEADY);
			corporatePrincess.setPortraitSprite("graphics/portraits/catherine.png");

			corporatePrincess.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2);
			corporatePrincess.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
			corporatePrincess.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2);
			corporatePrincess.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2);
			corporatePrincess.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 2);
			corporatePrincess.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2);
			corporatePrincess.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2);
			corporatePrincess.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 2);
			corporatePrincess.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
			corporatePrincess.getStats().setLevel(8);

			importantPeople.addPerson(corporatePrincess);

			market.getCommDirectory().addPerson(corporatePrincess, 0);
			market.addPerson(corporatePrincess);
		}
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

		BarEventManager barEventManager = BarEventManager.getInstance();

		// If the prerequisites for the quest have been met (optional) and the game isn't already aware of the bar event,
		// add it to the BarEventManager so that it shows up in bars
	}
}
