package me.rubenicos.mc.picospacos;

import me.rubenicos.mc.picospacos.core.data.Database;
import me.rubenicos.mc.picospacos.module.Locale;
import me.rubenicos.mc.picospacos.module.Settings;
import me.rubenicos.mc.picospacos.module.hook.HookLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class PicosPacos extends JavaPlugin {

    private static PicosPacos instance;

    private static Settings SETTINGS;

    public static PicosPacos get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        SETTINGS = new Settings("settings.yml");
        SETTINGS.listener(this::onSettingsReload);

        Locale.reload();
        Database.load(this);
        HookLoader.reload();
    }

    @Override
    public void onDisable() {
        HookLoader.unload();
        Database.unload();
    }

    public static Settings SETTINGS() {
        return SETTINGS;
    }

    private void onSettingsReload() {
        if (!SETTINGS.reload()) {
            getLogger().severe("Cannot reload settings.yml file! Check console.");
        }
        Locale.reload();
        Database.reload();
        HookLoader.reload();
    }
}
