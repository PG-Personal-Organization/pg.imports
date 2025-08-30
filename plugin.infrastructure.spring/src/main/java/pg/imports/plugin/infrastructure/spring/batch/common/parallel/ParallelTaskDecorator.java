package pg.imports.plugin.infrastructure.spring.batch.common.parallel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskDecorator;
import pg.lib.common.spring.storage.HeadersHolder;

import java.util.HashMap;

@RequiredArgsConstructor
public class ParallelTaskDecorator implements TaskDecorator {
    private final HeadersHolder headersHolder;

    @Override
    public @NonNull Runnable decorate(final @NonNull Runnable runnable) {
        var contextMap = new HashMap<>(headersHolder.getAllHeaders());
        return () -> {
            try {
                contextMap.forEach(headersHolder::putHeader);
                runnable.run();
            } finally {
                // clean after execution
                contextMap.keySet().forEach(h -> headersHolder.tryToGetHeader(h).ifPresent(v -> { }));
            }
        };
    }
}
