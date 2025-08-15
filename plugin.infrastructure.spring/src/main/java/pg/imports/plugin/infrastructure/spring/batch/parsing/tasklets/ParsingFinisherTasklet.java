package pg.imports.plugin.infrastructure.spring.batch.parsing.tasklets;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.annotation.Transactional;
import pg.kafka.sender.EventSender;
import pg.imports.plugin.api.data.ImportId;
import pg.imports.plugin.api.reason.ImportRejectionReasons;
import pg.imports.plugin.infrastructure.persistence.imports.ImportEntity;
import pg.imports.plugin.infrastructure.persistence.imports.ImportRepository;
import pg.imports.plugin.infrastructure.persistence.records.ImportRecordsEntity;
import pg.imports.plugin.infrastructure.persistence.records.RecordsRepository;
import pg.imports.plugin.infrastructure.processing.events.ImportParsingFinishedEvent;
import pg.imports.plugin.infrastructure.processing.events.RejectImportParsingEvent;
import pg.imports.plugin.infrastructure.spring.batch.common.JobUtil;

import java.util.Collection;
import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class ParsingFinisherTasklet implements Tasklet {
    private final ImportRepository importRepository;
    private final RecordsRepository recordsRepository;
    private final EventSender eventSender;

    @Override
    @Transactional
    public RepeatStatus execute(final @NonNull StepContribution contribution, final @NonNull ChunkContext chunkContext) throws Exception {
        ImportId importId = JobUtil.getImportId(contribution);
        ImportEntity parsingImport = importRepository.getParsingImport(importId.id());

        parsingImport.finishParsing();
        importRepository.save(parsingImport);

        var recordsPartitions = recordsRepository.findAllByParentImportId(parsingImport.getImportId().id());
        if (recordsPartitions.isEmpty()) {
            log.info("No records found for import {}, rejecting import.", parsingImport.getImportId());
            eventSender.sendEvent(RejectImportParsingEvent.of(parsingImport.getImportId(), ImportRejectionReasons.NO_RECORDS_FOUND));
            return RepeatStatus.FINISHED;
        }

        if (containsRejectedRecords(recordsPartitions)) {
            log.info("Rejected records found for import {}, rejecting import.", parsingImport.getImportId());
            var recordsIds = recordsPartitions.stream().map(ImportRecordsEntity::getErrorRecordIds).flatMap(Collection::stream).toList();
            eventSender.sendEvent(RejectImportParsingEvent.of(parsingImport.getImportId(), ImportRejectionReasons.FAILED_RECORDS_PARSING, recordsIds));
            return RepeatStatus.FINISHED;
        }

        eventSender.sendEvent(ImportParsingFinishedEvent.of(parsingImport.getImportId()));
        log.info("Import parsing {} finished", parsingImport);
        return RepeatStatus.FINISHED;
    }

    private boolean containsRejectedRecords(final List<ImportRecordsEntity> records) {
        return records.stream().map(ImportRecordsEntity::getErrorCount).reduce(0, Integer::sum) > 0;
    }
}
