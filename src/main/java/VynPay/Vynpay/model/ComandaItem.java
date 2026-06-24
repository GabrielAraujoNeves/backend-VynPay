package VynPay.Vynpay.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "comanda_itens")
public class ComandaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comanda_id")
    private Comanda comanda;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "cliente_comanda_id")
    private ClienteComanda clienteComanda;

    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal precoTotal;

    public void calcularTotal() {
        this.precoTotal = this.precoUnitario.multiply(BigDecimal.valueOf(this.quantidade));
    }
}