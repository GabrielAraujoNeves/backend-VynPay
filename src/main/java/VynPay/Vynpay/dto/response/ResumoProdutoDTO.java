package VynPay.Vynpay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ResumoProdutoDTO {
    private Long produtoId;
    private String nomeProduto;
    private BigDecimal preco;
    private Integer quantidadeVendida;
    private BigDecimal valorTotal;
}
