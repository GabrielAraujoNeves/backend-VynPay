package VynPay.Vynpay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProdutoPromocaoResponse {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal precoOriginal;
    private BigDecimal precoPromocional;
    private Double descontoPercent;
    private Boolean isInHappyHour;
}