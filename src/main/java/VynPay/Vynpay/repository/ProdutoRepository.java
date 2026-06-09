package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByCompanyId(Long companyId);

    List<Produto> findByCategoriaIdAndCompanyId(Long categoriaId, Long companyId);

    Optional<Produto> findByIdAndCompanyId(Long id, Long companyId);

    @Query("SELECT p FROM Produto p WHERE p.company.id = :companyId AND LOWER(p.nome) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Produto> searchByNome(@Param("companyId") Long companyId, @Param("search") String search);

    @Query("SELECT p FROM Produto p WHERE p.company.id = :companyId AND p.quantidade < :minimo")
    List<Produto> findProdutosComEstoqueBaixo(@Param("companyId") Long companyId, @Param("minimo") Integer minimo);
}