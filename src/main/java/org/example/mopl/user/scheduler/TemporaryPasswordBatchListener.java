package org.example.mopl.user.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TemporaryPasswordBatchListener implements StepExecutionListener {
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
       if (stepExecution.getExitStatus().getExitCode().equals(ExitStatus.COMPLETED.getExitCode())) {
           log.info("Temporary Password 정리 작업 성공. 삭제 수 : " + stepExecution.getReadCount());
       }
       else
       {
           log.warn("Temporary Password 정리 작업 실패. 원인 : " + stepExecution.getFailureExceptions());
       }

       return stepExecution.getExitStatus();
    }
}
