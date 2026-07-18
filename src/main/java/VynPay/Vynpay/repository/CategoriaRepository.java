package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.Categoria;
import VynPay.Vynpay.model.Categoria.TipoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    // Buscar por empresa
    List<Categoria> findByCompanyId(Long companyId);

    // Buscar por nome (exato)
    Optional<Categoria> findByNomeAndCompanyId(String nome, Long companyId);

    // Buscar por nome contendo (case insensitive)
    List<Categoria> findByCompanyIdAndNomeContainingIgnoreCase(Long companyId, String nome);

    // Buscar categorias ativas
    List<Categoria> findByCompanyIdAndIsAtivoTrue(Long companyId);

    // Buscar categorias pai (sem categoria pai)
    List<Categoria> findByCompanyIdAndCategoriaPaiIdIsNull(Long companyId);

    // Buscar categorias filhas
    List<Categoria> findByCompanyIdAndCategoriaPaiId(Long companyId, Long categoriaPaiId);

    // Buscar por tipo
    List<Categoria> findByCompanyIdAndTipoCategoria(Long companyId, TipoCategoria tipoCategoria);

    // Buscar por tipo e ativas
    List<Categoria> findByCompanyIdAndTipoCategoriaAndIsAtivoTrue(Long companyId, TipoCategoria tipoCategoria);

    // Verificar se existe categoria com mesmo nome (excluindo uma específica)
    @Query("SELECT COUNT(c) > 0 FROM Categoria c WHERE c.company.id = :companyId AND c.nome = :nome AND c.id != :id")
    boolean existsByNomeAndCompanyIdAndIdNot(@Param("nome") String nome,
                                             @Param("companyId") Long companyId,
                                             @Param("id") Long id);
}