package VynPay.Vynpay.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "estoque")
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nomeProduto;

    // REMOVA este campo String categoria
    // @Column(length = 50)
    // private String categoria;

    // ADICIONE o relacionamento com Categoria
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(length = 20)
    private String unidadeMedida;

    @Column(name = "peso_volume")
    private Double pesoVolume;

    @Column(name = "preco_unitario")
    private Double precoUnitario;

    @Column(name = "preco_compra")
    private Double precoCompra;

    @Column(name = "estoque_minimo")
    private Integer estoqueMinimo;

    @Column(name = "estoque_maximo")
    private Integer estoqueMaximo;

    @Column(name = "localizacao")
    private String localizacao;

    @Column(name = "fornecedor")
    private String fornecedor;

    @Column(name = "data_validade")
    private LocalDateTime dataValidade;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(length = 500)
    private String observacoes;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @PrePersist
    protected void onCreate() {
        dataCadastro = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (quantidade == null) {
            quantidade = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    // Métodos de negócio
    public void adicionarQuantidade(Integer quantidadeAdicionar) {
        if (quantidadeAdicionar != null && quantidadeAdicionar > 0) {
            this.quantidade += quantidadeAdicionar;
            this.dataAtualizacao = LocalDateTime.now();
        }
    }

    public void removerQuantidade(Integer quantidadeRemover) {
        if (quantidadeRemover != null && quantidadeRemover > 0) {
            if (this.quantidade >= quantidadeRemover) {
                this.quantidade -= quantidadeRemover;
                this.dataAtualizacao = LocalDateTime.now();
            } else {
                throw new IllegalArgumentException("Quantidade insuficiente em estoque! Disponível: " + this.quantidade);
            }
        }
    }

    public boolean isEstoqueBaixo() {
        return estoqueMinimo != null && quantidade <= estoqueMinimo;
    }

    public boolean isEstoqueAlto() {
        return estoqueMaximo != null && quantidade >= estoqueMaximo;
    }

    public boolean isVencido() {
        return dataValidade != null && dataValidade.isBefore(LocalDateTime.now());
    }

    public boolean isProximoVencer() {
        if (dataValidade == null) return false;
        LocalDateTime trintaDias = LocalDateTime.now().plusDays(30);
        return dataValidade.isBefore(trintaDias) && dataValidade.isAfter(LocalDateTime.now());
    }

    public Double getValorTotalEstoque() {
        if (precoUnitario != null && quantidade != null) {
            return precoUnitario * quantidade;
        }
        return 0.0;
    }
}