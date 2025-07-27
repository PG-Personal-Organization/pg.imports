package pg.plugin.infrastructure.spring.batch.parsing.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import pg.kafka.sender.EventSender;
import pg.plugin.api.rejection.reason.ImportRejectionReasons;
import pg.plugin.infrastructure.persistence.records.ImportRecordsEntity;
import pg.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.plugin.infrastructure.processing.events.RejectImportParsingEvent;
import pg.plugin.infrastructure.spring.batch.common.JobUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class ParsingErrorJobListener implements JobExecutionListener {
    private final EventSender eventSender;
    private final RecordsRepository recordsRepository;

    @Override
    public void afterJob(final JobExecution jobExecution) {
        if (!jobExecution.getStatus().isUnsuccessful()) {
            return;
        }

        try {
            var reason = getRejectReason(jobExecution);
            var importContext = JobUtil.getImportContext(jobExecution);
            log.info("Import {} rejected with reason {}, description: {}", importContext.getImportId(), reason, jobExecution.getExitStatus().getExitDescription());
            var recordsPartitions = recordsRepository.findAllByParentImportId(importContext.getImportId());
            var recordsIds = recordsPartitions.stream().map(ImportRecordsEntity::getErrorRecordIds).flatMap(Collection::stream).toList();
            eventSender.sendEvent(RejectImportParsingEvent.of(importContext.getImportId(), reason, recordsIds));
        } catch (final Exception e) {
            log.error("Error during job execution", e);
            throw new RuntimeException(e);
        }
    }

    private String getRejectReason(final JobExecution jobExecution) {
        return jobExecution.getStepExecutions().stream()
                .map(JobUtil::getRejectReason)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(reason -> reason, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ImportRejectionReasons.UNEXPECTED);
    }
}
