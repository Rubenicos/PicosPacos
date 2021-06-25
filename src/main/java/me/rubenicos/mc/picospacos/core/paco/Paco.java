package me.rubenicos.mc.picospacos.core.paco;

import me.rubenicos.mc.picospacos.PicosPacos;
import me.rubenicos.mc.picospacos.api.event.InventoryPacoEvent;
import me.rubenicos.mc.picospacos.api.event.ItemsPacoEvent;
import me.rubenicos.mc.picospacos.api.object.PacoRule;
import me.rubenicos.mc.picospacos.core.data.Database;
import me.rubenicos.mc.picospacos.module.Settings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Paco implements Listener {

    private final PicosPacos pl;
    private final Settings file;

    private final List<PacoRule> rules = new ArrayList<>();
    private final Map<UUID, List<ItemStack>> players = new HashMap<>();

    public Paco(PicosPacos pl) {
        this.pl = pl;
        file = new Settings("rules.yml", "rules.yml", false, false);
        file.listener(this::onRulesReload);
        pl.getServer().getPluginManager().registerEvents(this, pl);
    }

    public void disable() {
        players.forEach((player, items) -> Database.get().getPlayer(player).getItems().addAll(items));
        players.clear();
        rules.clear();
    }

    private void onRulesReload() {
        if (!file.reload()) {
            pl.getLogger().severe("Cannot reload rules.yml file");
        } else {
            // Load rules...
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (e.getKeepInventory()) return;

        if (Database.get().getPlayer(e.getEntity()).getSaves() > 0) {
            InventoryPacoEvent event = new InventoryPacoEvent(e.getEntity(), e.getDrops());
            pl.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                e.setKeepInventory(true);
                Database.get().getPlayer(e.getEntity()).reduceSaves(1);
                return;
            }
        }

        rules.forEach(rule -> {
            List<ItemStack> matches = new ArrayList<>();
            e.getDrops().forEach(item -> {
                if (rule.match(item)) {
                    matches.add(item);
                }
            });
            if (!matches.isEmpty()) {
                ItemsPacoEvent event = new ItemsPacoEvent(e.getEntity(), rule, matches);
                pl.getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    if (players.containsKey(e.getEntity().getUniqueId())) {
                        players.get(e.getEntity().getUniqueId()).addAll(event.getItems());
                    } else {
                        players.put(e.getEntity().getUniqueId(), event.getItems());
                    }
                }
            }
        });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (players.containsKey(e.getPlayer().getUniqueId())) {
            e.getPlayer().getInventory().addItem(players.get(e.getPlayer().getUniqueId()).toArray(new ItemStack[0]));
            players.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {

    }
}