package pg.plugin.infrastructure.spring.batch.importing.distributed;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;
import pg.kafka.consumer.MessageHandler;

@Log4j2
@RequiredArgsConstructor
public class DistributedImportChunkResponseHandler implements MessageHandler<ImportChunkMessageResponse> {
    private final PollableChannel responseChannel;

    @Override
    public void handleMessage(final @NonNull ImportChunkMessageResponse message) {
        var chunkResponse = message.getResponse();
        responseChannel.send(new GenericMessage<>(chunkResponse));
    }

    @Override
    public Class<ImportChunkMessageResponse> getMessageType() {
        return ImportChunkMessageResponse.class;
    }
}
