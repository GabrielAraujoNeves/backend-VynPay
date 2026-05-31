package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.CategoriaProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<CategoriaProduto, Long> {

    List<CategoriaProduto> findByCompanyId(Long companyId);

    Optional<CategoriaProduto> findByIdAndCompanyId(Long id, Long companyId);

    Optional<CategoriaProduto> findByNomeAndCompanyId(String nome, Long companyId);

    // ✅ CORRIGIDO: mude de "categoriaProduto" para "CategoriaProduto"
    @Query("SELECT c FROM CategoriaProduto c LEFT JOIN FETCH c.produtos WHERE c.id = :id AND c.company.id = :companyId")
    Optional<CategoriaProduto> findByIdWithProdutos(@Param("id") Long id, @Param("companyId") Long companyId);
}