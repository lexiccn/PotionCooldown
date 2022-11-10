package me.sudura.template.listeners;

import me.sudura.template.MainClass;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class PotionListener implements Listener {
    MainClass plugin;
    public PotionListener(MainClass instance) {
        plugin = instance;
    }

    @EventHandler
    public void onDrinkPotion (PlayerItemConsumeEvent event) {
        ItemMeta eventItemMeta = event.getItem().getItemMeta();
        if (eventItemMeta instanceof PotionMeta) {
            for (String potionType : plugin.getConfig().getStringList("potion")) {
                PotionType potionType2 = PotionType.valueOf(potionType);
                PotionMeta eventPotion = (PotionMeta)eventItemMeta;
                if (potionType2 == eventPotion.getBasePotionData().getType()) {
                    event.setCancelled(true);
                    plugin.getLogger().info(plugin.getMessages().getString("event.blocked.drink"));
                }
            }
        }
    }
}
