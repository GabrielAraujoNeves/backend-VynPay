package VynPay.Vynpay.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PulseiraProdutoResponse {
    private String message;
    private String numeroPulseira;
    private String nomeCliente;
    private String produtoNome;
    private Integer quantidade;
    private BigDecimal precoTotal;
    private BigDecimal novoSaldo;
}