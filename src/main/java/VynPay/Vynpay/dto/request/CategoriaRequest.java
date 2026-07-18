package VynPay.Vynpay.dto.request;


import lombok.Data;

@Data
public class CategoriaRequest {
    private String nome;
    private String descricao;
    private Long categoriaPaiId;
    private Boolean isAtivo;
}