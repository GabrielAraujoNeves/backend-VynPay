package VynPay.Vynpay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RemoverItemResponse {
    private String message;
    private String produtoNome;
    private Integer quantidadeRemovida;
    private BigDecimal precoUnitario;
    private BigDecimal precoTotal;
    private String clienteNome;
    private String justificativa;
    private String removidoPor;
}