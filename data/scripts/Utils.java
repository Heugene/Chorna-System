package data.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class Utils {

    public static Vector2f getUnoccupiedLocation(Vector2f center, List<StarSystemAPI> otherSystems, StarSystemAPI newSystem, float extraRadius) {
        Vector2f result = null;
        boolean isDone = false;
        float angle = 0f;
        float angleStep = 16f;
        float radius = 0f;
        float radiusStep = 1f;

        while (!isDone) {
            float x = center.getX() + (float) Math.cos(Math.toRadians(angle)) * radius;
            float y = center.getY() + (float) Math.sin(Math.toRadians(angle)) * radius;

            boolean isUnoccupied = true;
            for (StarSystemAPI s : otherSystems) {
                float dx = x - s.getLocation().getX();
                float dy = y - s.getLocation().getY();
                float minDistance = extraRadius + newSystem.getMaxRadiusInHyperspace() + s.getMaxRadiusInHyperspace();

                //squared Euclidean distance formula
                if (dx * dx + dy * dy < minDistance * minDistance) {
                    isUnoccupied = false;
                    break;
                }
            }

            if (isUnoccupied) {
                result = new Vector2f(x, y);
                isDone = true;
            } else {
                // the main idea here is to reduce calculations by such a way that it still
                // meaningfully checks evenly distributed locations
                // with an angle step of 16 degrees, it performs 22.5 checks per circle
                // decimal part causes an offset of angle on the second circle equal to 0.5*angleStep
                // so after 2 circles it performs a total of 45 evenly distributed checks with a difference of 8 degrees
                // in the original code that I took this algorithm from, 720 checks were needed per circle
                // original source: https://fractalsoftworks.com/forum/index.php?topic=25418.0
                if (angle >= 2*360) {
                    angle = 0f;
                    radius += radiusStep;
                } else {
                    angle += angleStep;
                }
            }
        }

        return result;
    }



    public static Constellation getNearestConstellation(Vector2f origin) {
        float minDistance = Float.MAX_VALUE;
        Constellation closest = null;

        for (Constellation constellation : getAllConstellations()) {
            float distance = Misc.getDistance(origin, constellation.getLocation());
            if (distance < minDistance) {
                minDistance = distance;
                closest = constellation;
            }
        }

        return closest;
    }

    public static Set<Constellation> getAllConstellations() {
        Set<Constellation> constellations = new HashSet<>();

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (!system.isInConstellation() || !system.isProcgen()) {
                continue;
            }
            Constellation c = system.getConstellation();
            if (c != null) {
                constellations.add(c);
            }
        }

        return constellations;
    }

    public static List<StarSystemAPI> getNearbyStarSystems(Vector2f center, float maxRangeLY) {
        List<StarSystemAPI> result = new ArrayList<>();

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            float distance = Misc.getDistanceLY(center, system.getLocation());
            if (distance > maxRangeLY) {
                continue;
            }
            result.add(system);
        }

        return result;
    }

    public static void clearHyperspaceNebulaAroundSystem(StarSystemAPI system) {
        // Clear nebula in hyperspace
        HyperspaceTerrainPlugin plugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin();
        NebulaEditor editor = new NebulaEditor(plugin);
        float minRadius = plugin.getTileSize() * 2f;
        float hyperspaceRadius = system.getMaxRadiusInHyperspace();
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, hyperspaceRadius + minRadius, 0f, 360f);
        editor.clearArc(system.getLocation().x, system.getLocation().y, 0f, hyperspaceRadius + minRadius, 0f, 360f, 0.25f);
    }
}
