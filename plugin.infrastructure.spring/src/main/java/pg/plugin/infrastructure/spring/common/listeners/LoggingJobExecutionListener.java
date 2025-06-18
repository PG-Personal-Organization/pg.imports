package pg.plugin.infrastructure.spring.common.listeners;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

import java.time.Duration;

@Log4j2
public class LoggingJobExecutionListener implements JobExecutionListener {

    @Override
    public void afterJob(final JobExecution jobExecution) {
        if (jobExecution.getStartTime() == null) {
            log.info("Job duration [id={}, name={}]: not recorded", jobExecution.getJobId(), jobExecution.getJobInstance().getJobName());
            return;
        }
        var duration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime());
        var durationText = DurationFormatUtils.formatDuration(duration.toMillis(), "H:mm:ss:SSS", true);
        log.info("Job duration [id={}, name={}]: {}", jobExecution.getJobId(), jobExecution.getJobInstance().getJobName(), durationText);
    }
}
