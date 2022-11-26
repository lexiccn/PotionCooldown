package me.sudura.potioncooldown.listeners;

import me.sudura.potioncooldown.PotionCooldown;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
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

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PotionListener implements Listener {
    PotionCooldown plugin;
    private final Map<UUID, Long> turtleCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> turtleBossbars = new ConcurrentHashMap<>();

    private final PotionData waterPot = new PotionData(PotionType.WATER, false, false);

    public PotionListener(PotionCooldown instance) {
        plugin = instance;
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> this.turtleCooldowns.keySet().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            float progress = getSecondsLeft(uuid) / 30f;
            if (progress == 0f) {
                this.turtleBossbars.get(uuid);
            }
            this.turtleBossbars.get(uuid).progress(progress);
        }), 0L, 20L);
    }

    private void setCooldown(Player player) {
        BossBar bossBar = BossBar.bossBar(Component.text("Turtle Master"), 1f, BossBar.Color.PINK, BossBar.Overlay.NOTCHED_20);
        player.showBossBar(bossBar);
        turtleCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        turtleBossbars.put(player.getUniqueId(), bossBar);
    }

    private long getSecondsLeft(UUID uuid) {
        long seconds = ((this.turtleCooldowns.get(uuid) / 1000L) + 30L) - (System.currentTimeMillis() / 1000L);
        if (seconds < 1L) {
            this.turtleCooldowns.remove(uuid);
            return 0L;
        }
        return seconds;
    }

    //Tipped Arrows
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileCollide (ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow && arrow.getBasePotionData().getType() == PotionType.TURTLE_MASTER) {
            if (!(event.getHitEntity() instanceof Player player)) return;
            if (turtleCooldowns.containsKey(player.getUniqueId())) {
                arrow.setBasePotionData(waterPot);
            } else {
                setCooldown(player);
            }
        }
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEnterCloud (AreaEffectCloudApplyEvent event) {
        if (event.getEntity().getBasePotionData().getType() == PotionType.TURTLE_MASTER) {
            Iterator<LivingEntity> entityIterator = event.getAffectedEntities().iterator();
            while (entityIterator.hasNext()) {
                LivingEntity ent = entityIterator.next();
                if (!(ent instanceof Player player)) continue;
                if (turtleCooldowns.containsKey(player.getUniqueId())) {
                    entityIterator.remove();
                } else {
                    setCooldown(player);
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
                    setCooldown(player);
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
                setCooldown(event.getPlayer());
            }
        }
    }
}
