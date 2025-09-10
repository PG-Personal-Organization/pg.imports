package pg.imports.plugin.infrastructure.spring.batch.common.distributed;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class DistributedResponseConsumerGroupProvider {
    private final Environment environment;

    public String getConsumerGroup(final @NonNull String topicName) {
        String app = Optional.ofNullable(environment.getProperty("spring.application.name")).orElse("app");
        String host = Optional.ofNullable(System.getenv("HOSTNAME")).orElse(UUID.randomUUID().toString());
        return app + "-" + topicName + "-" + host;
    }
}
