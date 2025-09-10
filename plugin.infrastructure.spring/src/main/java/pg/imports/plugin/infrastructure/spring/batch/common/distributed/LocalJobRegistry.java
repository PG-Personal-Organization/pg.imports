package pg.imports.plugin.infrastructure.spring.batch.common.distributed;

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class LocalJobRegistry {
    private final Set<Long> localJobs = ConcurrentHashMap.newKeySet();

    public void register(final long jobId) {
        localJobs.add(jobId);
    }

    public void unregister(final long jobId) {
        localJobs.remove(jobId);
    }

    public boolean isLocal(final long jobId) {
        return localJobs.contains(jobId);
    }
}
