package me.sudura.template;

import co.aikar.commands.PaperCommandManager;
import me.sudura.template.commands.PotionCommand;
import me.sudura.template.listeners.PotionListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MainClass extends JavaPlugin {
    private File messagesFile;
    private FileConfiguration messages;
    public void onEnable() {
        PaperCommandManager manager = new PaperCommandManager(this);
        this.saveDefaultMessages();
        this.saveDefaultConfig();
        manager.registerCommand(new PotionCommand(this));
        this.getServer().getPluginManager().registerEvents(new PotionListener(this), this);
    }

    public FileConfiguration getMessages() {
        return this.messages;
    }

    public void reloadMessages() {
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void saveDefaultMessages() {
        messagesFile = new File(getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }
}
