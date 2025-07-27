package pg.plugin.infrastructure.spring.batch.parsing.distributed;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.integration.chunk.ChunkRequest;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import pg.kafka.sender.EventSender;
import pg.plugin.infrastructure.spring.batch.common.distributed.ChunkSendingException;
import pg.plugin.infrastructure.spring.batch.parsing.processor.PartitionedRecord;

import java.util.concurrent.atomic.AtomicLong;

@Log4j2
@RequiredArgsConstructor
@StepScope
public class DistributedParseChunkSender implements ItemWriter<PartitionedRecord> {
    private final AtomicLong sequence = new AtomicLong();
    private final EventSender eventSender;
    private final long jobId;

    private StepExecution stepExecution;

    @BeforeStep
    @SuppressWarnings("HiddenField")
    public void beforeStep(final StepExecution stepExecution) {
        this.stepExecution = stepExecution;
    }

    @Override
    public void write(final Chunk<? extends PartitionedRecord> chunk) {
        var chunkRequest = new ChunkRequest<PartitionedRecord>((int) sequence.getAndIncrement(), chunk, jobId, stepExecution.createStepContribution());
        log.info("Sending chunk: {}", chunkRequest);
        try {
            var importChunkMessage = new ParseChunkMessageRequest(chunkRequest);
            eventSender.sendEvent(importChunkMessage);
        } catch (final Exception e) {
            log.error("Error during chunk sending", e);
            if (chunk.getItems().isEmpty()) {
                throw new IllegalArgumentException("Chunk is empty");
            }
            throw new ChunkSendingException(String.format("Error during chunk nr.%s sending", chunk.getItems().getFirst().getChunkNumber()), e);
        }
    }
}
