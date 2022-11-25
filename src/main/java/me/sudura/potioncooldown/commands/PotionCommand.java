package me.sudura.potioncooldown.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.sudura.potioncooldown.PotionCooldown;
import org.bukkit.command.CommandSender;

@CommandAlias("potioncooldown")
public class PotionCommand extends BaseCommand {
    PotionCooldown plugin;

    public PotionCommand(PotionCooldown instance){
        plugin = instance;
    }

    @Default
    public void onDefault(CommandSender sender) {
        sender.sendMessage("§cPlease specify a subcommand!");
    }

    @Subcommand("reload")
    @CommandPermission("sudura.potioncooldown.reload")
    @Description("Reloads the config and messages files")
    public void onReload(CommandSender sender) {
        plugin.reloadMessages();
        plugin.reloadConfig();
        sender.sendMessage("§3Reloaded the messages and config!");
    }

    @CatchUnknown
    public void onUnknown(CommandSender sender) {
        sender.sendMessage("§cUnknown subcommand!");
    }
}
