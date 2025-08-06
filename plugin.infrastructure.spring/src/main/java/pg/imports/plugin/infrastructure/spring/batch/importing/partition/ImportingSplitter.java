package pg.imports.plugin.infrastructure.spring.batch.importing.partition;

import lombok.NonNull;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.SimpleStepExecutionSplitter;
import org.springframework.batch.core.repository.JobRepository;

import java.util.Set;

public class ImportingSplitter extends SimpleStepExecutionSplitter {

    public ImportingSplitter(final JobRepository jobRepository, final String stepName, final Partitioner partitioner) {
        super(jobRepository, false, stepName, partitioner);
    }

    @NonNull
    @Override
    public Set<StepExecution> split(final @NonNull StepExecution stepExecution, final int gridSize) throws JobExecutionException {
        return super.split(stepExecution, gridSize);
    }
}
