
package VynPay.Vynpay.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "logs_remocao_itens")
public class LogRemocaoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "comanda_id")
    private Long comandaId;

    @Column(name = "cliente_comanda_id")
    private Long clienteComandaId;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "produto_nome")
    private String produtoNome;

    @Column(name = "preco_unitario")
    private BigDecimal precoUnitario;

    @Column(name = "quantidade")
    private Integer quantidade;

    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    @Column(name = "cliente_nome")
    private String clienteNome;

    @Column(name = "justificativa")
    private String justificativa;

    @Column(name = "removido_por")
    private String removidoPor;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "data_remocao")
    private LocalDateTime dataRemocao;

    @PrePersist
    protected void onCreate() {
        dataRemocao = LocalDateTime.now();
    }
}