package VynPay.Vynpay.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(name = "tipo_categoria")
    @Enumerated(EnumType.STRING)
    private TipoCategoria tipoCategoria; // PRODUTO ou ESTOQUE

    @Column(name = "categoria_pai_id")
    private Long categoriaPaiId;

    @Column(name = "is_ativo")
    private Boolean isAtivo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @JsonIgnore
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Produto> produtos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    private List<Estoque> itensEstoque = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isAtivo == null) {
            isAtivo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum TipoCategoria {
        PRODUTO,
        ESTOQUE
    }

    public boolean isCategoriaPai() {
        return categoriaPaiId == null;
    }

    public boolean isCategoriaFilha() {
        return categoriaPaiId != null;
    }

    public boolean isAtivo() {
        return isAtivo != null && isAtivo;
    }
}