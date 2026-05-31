package VynPay.Vynpay.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProdutoRequest {
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer quantidade;
    private Long categoriaId;
}