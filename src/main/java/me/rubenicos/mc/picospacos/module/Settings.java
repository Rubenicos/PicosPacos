package me.rubenicos.mc.picospacos.module;

import com.osiris.dyml.DYModule;
import com.osiris.dyml.DreamYaml;
import com.osiris.dyml.exceptions.DYReaderException;
import com.osiris.dyml.exceptions.DYWriterException;
import com.osiris.dyml.exceptions.DuplicateKeyException;
import com.osiris.dyml.exceptions.IllegalListException;
import com.osiris.dyml.watcher.DYFileEvent;
import com.osiris.dyml.watcher.DYFileEventListener;
import me.rubenicos.mc.picospacos.PicosPacos;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardWatchEventKinds;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings {

    private final PicosPacos pl = PicosPacos.get();
    private final Map<String, Object> cache = new HashMap<>();

    private String path;
    private final boolean update;
    private boolean defaultExists = true;

    private DreamYaml yaml;
    private DreamYaml defYaml;
    private DYFileEventListener<DYFileEvent> listener;

    public Settings(String path) {
        this(path, path, true, true);
    }

    public Settings(String path, String defPath, boolean requireDef, boolean update) {
        this.path = path;
        this.update = update;
        InputStream in = pl.getResource(path);
        if (in == null) {
            in = pl.getResource(defPath);
        }

        if (in == null) {
            if (requireDef) {
                Bukkit.getLogger().severe("Cannot find " + defPath + " file on plugin JAR!");
                pl.getPluginLoader().disablePlugin(pl);
                return;
            }
            defaultExists = false;
        } else {
            try {
                defYaml = new DreamYaml(in);
            } catch (IllegalListException | IOException | DuplicateKeyException | DYReaderException e) {
                e.printStackTrace();
                defaultExists = false;
            }
            if (requireDef && !defaultExists) {
                Bukkit.getLogger().severe("Cannot load " + defPath + " file on plugin JAR!");
                pl.getPluginLoader().disablePlugin(pl);
            }
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void reloadDefault(String path) {
        InputStream in = pl.getResource(this.path);
        if (in == null) {
            in = pl.getResource(path);
        }
        if (in != null) {
            try {
                defYaml = new DreamYaml(in);
                defaultExists = true;
            } catch (IllegalListException | IOException | DuplicateKeyException | DYReaderException e) {
                Bukkit.getLogger().severe("Cannot load " + path + " file on plugin JAR!");
                e.printStackTrace();
                defaultExists = false;
            }
        }
    }

    public boolean reload() {
        cache.clear();
        if (yaml != null && listener != null) {
            yaml.removeFileEventListener(listener);
        }
        String path = pl.getDataFolder() + File.separator + this.path;
        File file = new File(path);
        if (!file.exists()) {
            try {
                pl.saveResource(this.path, false);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return false;
            }
        }
        try {
            yaml = new DreamYaml(file);
        } catch (IllegalListException | IOException | DuplicateKeyException | DYReaderException e) {
            Bukkit.getLogger().severe("Cannot load " + this.path + " file on plugin folder!");
            e.printStackTrace();
            return false;
        }

        if (defaultExists && update) {
            yaml.getAllInEdit().clear();
            yaml.getAllInEdit().addAll(defYaml.getAllLoaded());
            try {
                yaml.saveAndLoad();
            } catch (IllegalListException | IOException | DYWriterException | DuplicateKeyException | DYReaderException e) {
                Bukkit.getLogger().severe("Cannot update " + this.path + " file on plugin folder!");
                e.printStackTrace();
            }
        }
        if (listener != null) {
            addListener();
        }
        return true;
    }

    public void listener(Runnable runnable) {
        this.listener = (event) -> {
            if (event.getWatchEventKind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                Bukkit.getScheduler().runTaskAsynchronously(pl, runnable);
            }
        };
        addListener();
    }

    private void addListener() {
        try {
            yaml.addFileEventListener(listener);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public DYModule get(String path) {
        return yaml.get(path.split("\\."));
    }

    @NotNull
    public String getString(@NotNull String path) {
        return String.valueOf(cache.getOrDefault(path, cache(path, getString0(path))));
    }

    private Object getString0(String path) {
        DYModule module = get(path);
        if (module == null) {
            return null;
        } else {
            return module.asString();
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public List<String> getStringList(@NotNull String path) {
        return (List<String>) cache.getOrDefault(path, cache(path, getStringList0(path)));
    }

    private Object getStringList0(String path) {
        DYModule module = get(path);
        if (module == null) {
            return Collections.emptyList();
        } else {
            return module.asStringList();
        }
    }

    public int getInt(@NotNull String path) {
        return (int) cache.getOrDefault(path, cache(path, getInt0(path)));
    }

    public Object getInt0(String path) {
        DYModule module = get(path);
        if (module == null) {
            return -1;
        } else {
            return module.asInt();
        }
    }

    public boolean getBoolean(@NotNull String path) {
        return (boolean) cache.getOrDefault(path, cache(path, getBoolean0(path)));
    }

    private Object getBoolean0(String path) {
        DYModule module = get(path);
        if (module == null) {
            return false;
        } else {
            return module.asBoolean();
        }
    }

    private Object cache(String path, Object obj) {
        cache.put(path, obj);
        return obj;
    }
}