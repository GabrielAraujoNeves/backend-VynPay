package VynPay.Vynpay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoResponse {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer quantidade;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CategoriaInfo categoria;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaInfo {
        private Long id;
        private String nome;
        private String descricao;
    }
}