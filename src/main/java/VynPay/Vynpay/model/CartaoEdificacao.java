package VynPay.Vynpay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cartoes_edificacao")
public class CartaoEdificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cartao", unique = true, nullable = false)
    private String numeroCartao;

    @Column(name = "nome_cliente")
    private String nomeCliente;

    @Column(name = "cartao_vinculado")
    private String cartaoVinculado;

    @Column(name = "is_ativo")
    private Boolean isAtivo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}