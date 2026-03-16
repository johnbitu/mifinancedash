package dev.project.finance.services;

import dev.project.finance.models.AuditLog;
import dev.project.finance.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    // @Async para não bloquear a thread da requisição
    @Async
    public void registrar(String acao, Long userId, String ip, boolean sucesso, String detalhes) {
        try {
            AuditLog log = AuditLog.builder()
                    .acao(acao)
                    .userId(userId)
                    .ip(ip)
                    .sucesso(sucesso)
                    .detalhes(detalhes)
                    .ocorreuEm(LocalDateTime.now())
                    .build();

            auditLogRepository.save(log);
        } catch (Exception ex) {
            // Log de auditoria nunca pode derrubar a requisição principal
            log.error("Falha ao registrar audit log: acao={}, userId={}", acao, userId, ex);
        }
    }
}