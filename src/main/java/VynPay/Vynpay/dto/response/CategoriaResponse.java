package VynPay.Vynpay.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoriaResponse {
    private Long id;
    private String nome;
    private String descricao;
    private Long categoriaPaiId;
    private String nomeCategoriaPai;
    private Boolean isAtivo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long quantidadeProdutos;
    private Long quantidadeEstoque;
    private List<CategoriaResponse> subcategorias;
    private Boolean isCategoriaPai;
    private Boolean isCategoriaFilha;
}