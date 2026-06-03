package VynPay.Vynpay.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProdutoCompletoResponse {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;           // Preço promocional (se Happy Hour ativo) ou original
    private BigDecimal precoOriginal;   // Preço original (sempre visível)
    private Double descontoPercent;     // Percentual de desconto aplicado
    private Boolean isInHappyHour;      // Se o produto está em promoção
    private Integer quantidade;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CategoriaInfo categoria;

    @Data
    public static class CategoriaInfo {
        private Long id;
        private String nome;
        private String descricao;

        public CategoriaInfo() {}

        public CategoriaInfo(Long id, String nome, String descricao) {
            this.id = id;
            this.nome = nome;
            this.descricao = descricao;
        }
    }
}