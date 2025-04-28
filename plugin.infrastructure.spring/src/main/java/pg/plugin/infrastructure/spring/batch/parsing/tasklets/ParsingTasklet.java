package pg.plugin.infrastructure.spring.batch.parsing.tasklets;

import lombok.NonNull;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class ParsingTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(final @NonNull StepContribution contribution, final @NonNull ChunkContext chunkContext) throws Exception {
        return null;
    }
}
