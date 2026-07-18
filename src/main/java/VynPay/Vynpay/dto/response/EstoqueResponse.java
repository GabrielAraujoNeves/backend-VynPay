package VynPay.Vynpay.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EstoqueResponse {
    private Long id;
    private String nomeProduto;
    private Long categoriaId;
    private String categoriaNome;
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
    private LocalDateTime dataCadastro;
    private LocalDateTime dataAtualizacao;
    private String observacoes;
    private Double valorTotal;
    private Boolean isEstoqueBaixo;
    private Boolean isEstoqueAlto;
    private Boolean isVencido;
    private Boolean isProximoVencer;
}