package pg.imports.plugin.infrastructure.spring.batch.parsing.distributed;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.integration.chunk.ChunkResponse;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.GenericMessage;
import pg.kafka.consumer.MessageHandler;

@Log4j2
@RequiredArgsConstructor
public class DistributedParseChunkResponseHandler implements MessageHandler<ParseChunkMessageResponse> {
    private final PollableChannel responseChannel;

    @Override
    public void handleMessage(final @NonNull ParseChunkMessageResponse message) {
        var fakeExec = new StepExecution("remote-chunk", new JobExecution(message.getJobId()));
        var contrib = new StepContribution(fakeExec);

        var chunkResponse = new ChunkResponse(message.isSuccess(), message.getSequence(), message.getJobId(), contrib, message.getError());
        log.info("Forwarding ChunkResponse seq={} jobId={} status={}",
                chunkResponse.getSequence(), chunkResponse.getJobId(), chunkResponse.isSuccessful());
        responseChannel.send(new GenericMessage<>(chunkResponse));
    }

    @Override
    public Class<ParseChunkMessageResponse> getMessageType() {
        return ParseChunkMessageResponse.class;
    }
}
