package pg.plugin.infrastructure.spring.batch.common;

import lombok.experimental.UtilityClass;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import pg.plugin.api.data.ImportContext;
import pg.plugin.api.data.ImportId;
import pg.plugin.api.data.PluginCode;
import pg.plugin.api.strategies.RecordsStoringStrategy;
import pg.plugin.infrastructure.spring.common.config.KafkaImportsMessageStrategy;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@UtilityClass
public class JobUtil {
    public static final String IMPORT_ID_KEY = "IMPORT_ID";
    public static final String PLUGIN_CODE_KEY = "PLUGIN_CODE";
    public static final String FILE_ID_KEY = "FILE_ID";
    public static final String IMPORT_CONTEXT_KEY = "IMPORT_CONTEXT";
    public static final String KAFKA_IMPORTS_MESSAGE_STRATEGY_KEY = "KAFKA_IMPORTS_MESSAGE_STRATEGY";
    public static final String RECORDS_STORING_STRATEGY_KEY = "RECORDS_STORING_STRATEGY";
    public static final String REJECT_REASON_KEY = "REJECT_REASON";

    public ImportId getImportId(final StepContribution contribution) {
        String importId = contribution.getStepExecution().getJobParameters().getString(IMPORT_ID_KEY);
        return new ImportId(Objects.requireNonNull(importId));
    }

    public ImportId getImportId(final Map<String, Object> jobParameters) {
        return new ImportId((String) jobParameters.get(IMPORT_ID_KEY));
    }

    public PluginCode getPluginCode(final Map<String, Object> jobParameters) {
        return new PluginCode((String) jobParameters.get(PLUGIN_CODE_KEY));
    }

    public PluginCode getPluginCode(final StepContribution contribution) {
        String code = contribution.getStepExecution().getJobParameters().getString(PLUGIN_CODE_KEY);
        return new PluginCode(Objects.requireNonNull(code));
    }

    public PluginCode getPluginCode(final StepExecution stepExecution) {
        String code = stepExecution.getJobParameters().getString(PLUGIN_CODE_KEY);
        return new PluginCode(Objects.requireNonNull(code));
    }

    public void putImportContext(final StepContribution contribution, final ImportContext importContext) {
        contribution.getStepExecution().getJobExecution().getExecutionContext().put(IMPORT_CONTEXT_KEY, importContext);
    }

    public ImportContext getImportContext(final StepExecution stepExecution) {
        return (ImportContext) stepExecution.getJobExecution().getExecutionContext().get(IMPORT_CONTEXT_KEY);
    }

    public ImportContext getImportContext(final JobExecution jobExecution) {
        return (ImportContext) jobExecution.getExecutionContext().get(IMPORT_CONTEXT_KEY);
    }

    public void putFileId(final StepContribution contribution, final UUID fileId) {
        contribution.getStepExecution().getJobExecution().getExecutionContext().put(FILE_ID_KEY, fileId);
    }

    public UUID getFileId(final StepContribution contribution) {
        return UUID.fromString(contribution.getStepExecution().getJobExecution().getExecutionContext().getString(FILE_ID_KEY));
    }

    public UUID getFileId(final StepExecution stepExecution) {
        return UUID.fromString(stepExecution.getJobExecution().getExecutionContext().getString(FILE_ID_KEY));
    }

    public void putKafkaImportsMessageStrategy(final StepContribution contribution, final KafkaImportsMessageStrategy strategy) {
        contribution.getStepExecution().getJobExecution().getExecutionContext().put(KAFKA_IMPORTS_MESSAGE_STRATEGY_KEY, strategy);
    }

    public KafkaImportsMessageStrategy getKafkaImportsMessageStrategy(final StepContribution contribution) {
        return (KafkaImportsMessageStrategy) contribution.getStepExecution().getJobExecution().getExecutionContext().get(KAFKA_IMPORTS_MESSAGE_STRATEGY_KEY);
    }

    public void putRecordsStoringStrategy(final StepContribution contribution, final RecordsStoringStrategy strategy) {
        contribution.getStepExecution().getJobExecution().getExecutionContext().put(RECORDS_STORING_STRATEGY_KEY, strategy);
    }

    public RecordsStoringStrategy getRecordsStoringStrategy(final StepExecution stepExecution) {
        return (RecordsStoringStrategy) stepExecution.getExecutionContext().get(RECORDS_STORING_STRATEGY_KEY);
    }

    public String getRejectReason(final StepExecution stepExecution) {
        return (String) stepExecution.getJobExecution().getExecutionContext().get(REJECT_REASON_KEY);
    }

    public void putRejectReason(final StepExecution stepExecution, final String reason) {
        stepExecution.getJobExecution().getExecutionContext().put(REJECT_REASON_KEY, reason);
    }

}
