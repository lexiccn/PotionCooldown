package me.sudura.potioncooldown.listeners;

import com.destroystokyo.paper.event.entity.ProjectileCollideEvent;
import me.sudura.potioncooldown.PotionCooldown;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PotionListener implements Listener {
    PotionCooldown plugin;
    private final Map<UUID, Long> turtleCooldowns = new ConcurrentHashMap<>();

    private final PotionData waterPot = new PotionData(PotionType.WATER, false, false);

    public PotionListener(PotionCooldown instance) {
        plugin = instance;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> this.turtleCooldowns.keySet().forEach(uuid -> {
            if (getSecondsLeft(uuid) < 1L) {
                this.turtleCooldowns.remove(uuid);
            }
        }), 0L, 20L);
    }

    private long getSecondsLeft(UUID uuid) {
        return ((this.turtleCooldowns.get(uuid) / 1000L) + 10L) - (System.currentTimeMillis() / 1000L);
    }

    //Tipped Arrows
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileCollide (ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow && arrow.getBasePotionData().getType() == PotionType.TURTLE_MASTER) {
            if (!(event.getHitEntity() instanceof Player player)) return;
            if (turtleCooldowns.containsKey(player.getUniqueId())) {
                arrow.setBasePotionData(waterPot);
            } else {
                turtleCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEnterCloud (AreaEffectCloudApplyEvent event) {
        if (event.getEntity().getBasePotionData().getType() == PotionType.TURTLE_MASTER) {
            for (LivingEntity ent : event.getAffectedEntities()) {
                if (!(ent instanceof Player player)) continue;
                if (turtleCooldowns.containsKey(player.getUniqueId())) {
                    event.getAffectedEntities().remove(ent);
                } else {
                    turtleCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                }
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPotionSplash (PotionSplashEvent event) {
        if (event.getPotion().getPotionMeta().getBasePotionData().getType() == PotionType.TURTLE_MASTER) {
            for (LivingEntity ent : event.getAffectedEntities()) {
                if (!(ent instanceof Player player)) continue;
                if (turtleCooldowns.containsKey(player.getUniqueId())) {
                    event.setIntensity(ent, 0);
                } else {
                    turtleCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                }
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onDrinkPotion (PlayerItemConsumeEvent event) {
        if (event.getItem().getItemMeta() instanceof PotionMeta eventPotion && eventPotion.getBasePotionData().getType() == PotionType.TURTLE_MASTER) {
            if (turtleCooldowns.containsKey(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
            } else {
                turtleCooldowns.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
            }
        }
    }
}
