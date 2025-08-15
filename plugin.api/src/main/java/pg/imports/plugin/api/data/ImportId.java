package pg.imports.plugin.api.data;

import lombok.*;

import java.io.Serializable;

public record ImportId(@NonNull String id) implements Serializable {
}
