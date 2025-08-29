package dev.rm20.mcfireworkshow.show.Effects;

import dev.rm20.mcfireworkshow.MCFireworkShow;
import fr.skytasul.guardianbeam.Laser;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class GuardianBeamEffect {
    public static void displayBeam(Location start, Location end, int duration, int distance, boolean isCrystal) throws ReflectiveOperationException {
        var plugin = MCFireworkShow.showManager.getFireworkShowPlugin();
        if (!plugin.isEnabled()) {
            Bukkit.getLogger().severe("Plugin is not enabled, cannot run text action.");
            return;
        }
        Laser laser;
        if (isCrystal) {
            laser = new Laser.CrystalLaser(start, end, duration, distance);
        } else {
            laser = new Laser.GuardianLaser(start, end, duration, distance);
        }
        laser.start(plugin);
    }
}
