package me.sudura.potioncooldown;

import me.sudura.potioncooldown.listeners.PotionListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PotionCooldown extends JavaPlugin {
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new PotionListener(this), this);
    }
}
