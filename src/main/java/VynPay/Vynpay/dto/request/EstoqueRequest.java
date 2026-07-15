package VynPay.Vynpay.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EstoqueRequest {
    private String nomeProduto;
    private String categoria;
    private Integer quantidade;
    private String unidadeMedida;
    private Double pesoVolume;
    private Double precoUnitario;
    private Double precoCompra;
    private Integer estoqueMinimo;
    private Integer estoqueMaximo;
    private String localizacao;
    private String fornecedor;
    private LocalDateTime dataValidade;
    private String observacoes;
    private Long produtoId; // ID do produto existente (opcional)
}