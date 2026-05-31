package VynPay.Vynpay.dto.request;

import lombok.Data;

@Data
public class AdicionarProdutoRequest {
    private Long produtoId;
    private Integer quantidade;
}