package pg.plugin.infrastructure.spring.batch.parsing.tasklets;

import lombok.NonNull;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.annotation.Transactional;

public class ParsingTasklet implements Tasklet {

    @Override
    @Transactional
    public RepeatStatus execute(final @NonNull StepContribution contribution, final @NonNull ChunkContext chunkContext) throws Exception {
        return RepeatStatus.FINISHED;
    }
}
