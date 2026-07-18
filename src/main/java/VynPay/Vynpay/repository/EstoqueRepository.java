package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    // Buscar por empresa
    List<Estoque> findByCompanyId(Long companyId);

    // Buscar por nome (contém)
    List<Estoque> findByCompanyIdAndNomeProdutoContainingIgnoreCase(Long companyId, String nome);

    // Buscar por categoria (ID da categoria)
    List<Estoque> findByCompanyIdAndCategoriaId(Long companyId, Long categoriaId);

    // Buscar por categoria (nome da categoria)
    @Query("SELECT e FROM Estoque e WHERE e.company.id = :companyId AND LOWER(e.categoria.nome) LIKE LOWER(CONCAT('%', :categoriaNome, '%'))")
    List<Estoque> findByCompanyIdAndCategoriaNomeContainingIgnoreCase(@Param("companyId") Long companyId,
                                                                      @Param("categoriaNome") String categoriaNome);

    // Buscar produtos com estoque baixo
    @Query("SELECT e FROM Estoque e WHERE e.company.id = :companyId AND e.estoqueMinimo IS NOT NULL AND e.quantidade <= e.estoqueMinimo")
    List<Estoque> findProdutosComEstoqueBaixo(@Param("companyId") Long companyId);

    // Buscar produtos vencidos
    @Query("SELECT e FROM Estoque e WHERE e.company.id = :companyId AND e.dataValidade < :dataAtual")
    List<Estoque> findProdutosVencidos(@Param("companyId") Long companyId, @Param("dataAtual") LocalDateTime dataAtual);

    // Buscar produtos próximos a vencer
    @Query("SELECT e FROM Estoque e WHERE e.company.id = :companyId AND e.dataValidade BETWEEN :dataAtual AND :dataLimite")
    List<Estoque> findProdutosProximosVencer(@Param("companyId") Long companyId,
                                             @Param("dataAtual") LocalDateTime dataAtual,
                                             @Param("dataLimite") LocalDateTime dataLimite);

    // Buscar por fornecedor
    List<Estoque> findByCompanyIdAndFornecedorContainingIgnoreCase(Long companyId, String fornecedor);

    // Contar total de itens por categoria
    @Query("SELECT e.categoria.nome, SUM(e.quantidade) FROM Estoque e WHERE e.company.id = :companyId GROUP BY e.categoria.nome")
    List<Object[]> sumQuantidadePorCategoria(@Param("companyId") Long companyId);

    // Buscar produtos com estoque acima do máximo
    @Query("SELECT e FROM Estoque e WHERE e.company.id = :companyId AND e.estoqueMaximo IS NOT NULL AND e.quantidade >= e.estoqueMaximo")
    List<Estoque> findProdutosEstoqueExcedente(@Param("companyId") Long companyId);

    // Calcular valor total do estoque por empresa
    @Query("SELECT SUM(e.precoUnitario * e.quantidade) FROM Estoque e WHERE e.company.id = :companyId")
    Double calcularValorTotalEstoque(@Param("companyId") Long companyId);

    // ========== MÉTODOS REMOVIDOS ==========
    // REMOVIDO: findProdutosEstoqueByTipoCategoria - não existe mais tipoCategoria
    // REMOVIDO: findProdutosProdutoByTipoCategoria - não existe mais tipoCategoria
}