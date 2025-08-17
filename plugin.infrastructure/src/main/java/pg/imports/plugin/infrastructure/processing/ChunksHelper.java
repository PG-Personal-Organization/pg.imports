package pg.imports.plugin.infrastructure.processing;

import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ChunksHelper {
    public static void forEachChunk(final List<String> ids, final int chunkSize, final java.util.function.Consumer<List<String>> consumer) {
        for (int from = 0; from < ids.size(); from += chunkSize) {
            int to = Math.min(from + chunkSize, ids.size());
            consumer.accept(ids.subList(from, to));
        }
    }
}
