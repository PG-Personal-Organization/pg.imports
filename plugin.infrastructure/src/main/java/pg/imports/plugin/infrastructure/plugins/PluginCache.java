package pg.imports.plugin.infrastructure.plugins;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import pg.imports.plugin.api.ImportPlugin;
import pg.imports.plugin.api.data.PluginCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PluginCache {
    private final Map<PluginCode, ImportPlugin> plugins = new HashMap<>();

    public PluginCache(final List<ImportPlugin> importPlugins) {
        importPlugins.forEach(plugin -> {
            if (plugins.containsKey(plugin.getCode())) {
                throw new DuplicateImportPluginException(String.format("Duplicate plugin code detected: " + plugin.getCode()));
            }
            plugins.put(plugin.getCode(), plugin);
        });
    }

    public Optional<ImportPlugin> tryGetPlugin(final @NonNull PluginCode pluginCode) {
        return Optional.ofNullable(plugins.get(pluginCode));
    }

    public ImportPlugin getPlugin(final @NonNull PluginCode pluginCode) {
        return Optional.ofNullable(plugins.get(pluginCode))
                .orElseThrow(() -> new ImportPluginNotFoundException(String.format("Import plugin with code %s not found", pluginCode)));
    }
}
