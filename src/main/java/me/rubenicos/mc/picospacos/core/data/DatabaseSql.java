package me.rubenicos.mc.picospacos.core.data;

import me.rubenicos.mc.picospacos.api.object.PlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class DatabaseSql extends Database {

    @Override
    boolean init() {
        return true;
    }

    @Override
    void enable() {

    }

    @Override
    void disable() {

    }

    @Override
    void save(PlayerData data) {

    }

    @Override
    PlayerData get(String name, String uuid) {
        return null;
    }
}
