package dev.project.finance.jobs;

import dev.project.finance.models.Recurrence;
import dev.project.finance.services.AuditService;
import dev.project.finance.services.GoalService;
import dev.project.finance.services.RecurrenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledJobs {

    private final RecurrenceService recurrenceService;
    private final GoalService goalService;
    private final AuditService auditService;

    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void processarRecorrencias() {
        List<Recurrence> pendentes = recurrenceService.listarPendentesExecucao(LocalDate.now());
        int executadas = 0;
        int falhas = 0;

        for (Recurrence recurrence : pendentes) {
            try {
                RecurrenceService.ExecutionResult result = recurrenceService.executar(recurrence);
                if (result.executada()) {
                    executadas++;
                }
                if (result.falhou()) {
                    falhas++;
                }
            } catch (Exception ex) {
                falhas++;
                log.warn("Falha ao processar recorrencia {}", recurrence.getId(), ex);
            }
        }

        log.info("Processamento de recorrencias finalizado. executadas={}, falhas={}", executadas, falhas);
        auditService.registrar("JOB_RECORRENCIAS", null, "SYSTEM", true,
                "Executadas=" + executadas + ", falhas=" + falhas);
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void expirarMetas() {
        try {
            int expiradas = goalService.expirarMetasVencidas();
            log.info("Job de expiracao de metas finalizado. expiradas={}", expiradas);
            auditService.registrar("JOB_EXPIRAR_METAS", null, "SYSTEM", true,
                    "Metas expiradas=" + expiradas);
        } catch (Exception ex) {
            log.error("Falha no job de expiracao de metas", ex);
            auditService.registrar("JOB_EXPIRAR_METAS", null, "SYSTEM", false, ex.getMessage());
        }
    }
}
