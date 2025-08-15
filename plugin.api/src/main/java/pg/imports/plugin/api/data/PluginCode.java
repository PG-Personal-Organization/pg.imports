package pg.imports.plugin.api.data;


import lombok.*;

import java.io.Serializable;

public record PluginCode(@NonNull String code) implements Serializable {
}
