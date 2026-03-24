package dev.project.finance.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "cards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Card extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "bandeira", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private CardBandeira bandeira;

    @Column(name = "tipo", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CardType tipo;

    @Column(name = "limite", precision = 15, scale = 2)
    private BigDecimal limite;

    @Column(name = "limite_disponivel", precision = 15, scale = 2)
    private BigDecimal limiteDisponivel;

    @Column(name = "dia_fechamento")
    private Integer diaFechamento;

    @Column(name = "dia_vencimento")
    private Integer diaVencimento;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;
}
