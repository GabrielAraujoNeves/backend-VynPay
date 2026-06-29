package VynPay.Vynpay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ItemCanceladoDTO {
    private Long itemId;
    private String produtoNome;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal valorTotal;
    private String clienteNome;
    private String justificativa;
    private String removidoPor;
    private LocalDateTime dataRemocao;
}
