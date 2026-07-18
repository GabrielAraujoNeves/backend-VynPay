package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Buscar produtos por empresa
    List<Produto> findByCompanyId(Long companyId);

    // Buscar produtos por categoria e empresa
    List<Produto> findByCategoriaIdAndCompanyId(Long categoriaId, Long companyId);

    // Buscar produto por ID e empresa
    Optional<Produto> findByIdAndCompanyId(Long id, Long companyId);

    // Buscar produtos por nome (contém)
    @Query("SELECT p FROM Produto p WHERE p.company.id = :companyId AND LOWER(p.nome) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Produto> searchByNome(@Param("companyId") Long companyId, @Param("search") String search);

    // Buscar produtos com estoque baixo
    @Query("SELECT p FROM Produto p WHERE p.company.id = :companyId AND p.quantidade < :minimo")
    List<Produto> findProdutosComEstoqueBaixo(@Param("companyId") Long companyId, @Param("minimo") Integer minimo);

    // ========== MÉTODOS REMOVIDOS ==========
    // REMOVIDO: findProdutosByTipoCategoriaProduto - não existe mais tipoCategoria

    // ========== MÉTODOS ADICIONAIS ==========
    // Buscar produtos por categoria (sem empresa)
    List<Produto> findByCategoriaId(Long categoriaId);
}