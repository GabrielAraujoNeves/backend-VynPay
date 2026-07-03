package VynPay.Vynpay.model;

import com.fasterxml.jackson.annotation.JsonIgnore;  // ← IMPORTAÇÃO NECESSÁRIA
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pulseiras")
public class Pulseira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_pulseira", unique = true, nullable = false)
    private String numeroPulseira;

    @Column(name = "nome_cliente")
    private String nomeCliente;

    @Column(name = "pulseira_agrupada_com")
    private String pulseiraAgrupadaCom;

    @Column(name = "is_ativo")
    private Boolean isAtivo = true;

    @JsonIgnore
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