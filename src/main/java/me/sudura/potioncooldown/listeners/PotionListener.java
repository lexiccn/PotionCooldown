package me.sudura.potioncooldown.listeners;

import me.sudura.potioncooldown.PotionCooldown;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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
        Bukkit.getScheduler().runTaskTimer(plugin, () -> this.turtleCooldowns.keySet().forEach(uuid -> {
            BossBar bossBar = this.turtleBossbars.get(uuid);
            long seconds = getSecondsLeft(uuid);
            if (seconds < 1L) {
                this.turtleCooldowns.remove(uuid);
                bossBar.removeAll();
                this.turtleBossbars.remove(uuid);
                return;
            }

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && !bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }

            bossBar.setProgress(seconds / 1200d);
        }), 0L, 20L);
    }

    private long getSecondsLeft(UUID uuid) {
        return ((this.turtleCooldowns.get(uuid) / 1000L) + 1200L) - (System.currentTimeMillis() / 1000L);
    }

    private void setCooldown(UUID uuid) {
        this.turtleBossbars.put(uuid, Bukkit.createBossBar("Turtle Master Cooldown", BarColor.YELLOW, BarStyle.SEGMENTED_20));
        this.turtleCooldowns.put(uuid, System.currentTimeMillis());
    }

    //Tipped Arrows
    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileCollide (ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow && arrow.getBasePotionData().getType() == PotionType.TURTLE_MASTER) {
            if (!(event.getHitEntity() instanceof Player player)) return;
            if (turtleCooldowns.containsKey(player.getUniqueId())) {
                arrow.setBasePotionData(waterPot);
            } else {
                setCooldown(player.getUniqueId());
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
                    setCooldown(player.getUniqueId());
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
                    setCooldown(player.getUniqueId());
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
                setCooldown(event.getPlayer().getUniqueId());
            }
        }
    }
}
