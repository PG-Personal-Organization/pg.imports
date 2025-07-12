package pg.plugin.infrastructure.spring.batch.importing.tasklets;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import pg.plugin.infrastructure.persistence.imports.ImportRepository;

@Log4j2
@RequiredArgsConstructor
public class ImportingInitializerTasklet implements Tasklet {
    private final ImportRepository importRepository;

    @Override
    public RepeatStatus execute(final @NonNull StepContribution contribution, final @NonNull ChunkContext chunkContext) throws Exception {
        return null;
    }
}
