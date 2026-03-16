package dev.project.finance.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "acao", nullable = false, length = 100)
    private String acao;

    @Column(name = "detalhes", columnDefinition = "TEXT")
    private String detalhes;

    @Column(name = "ip", length = 45)
    private String ip;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ocorreu_em", nullable = false)
    private LocalDateTime ocorreuEm;

    @Column(name = "sucesso", nullable = false)
    private Boolean sucesso;
}