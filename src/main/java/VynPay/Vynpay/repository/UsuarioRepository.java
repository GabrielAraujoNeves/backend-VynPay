package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<usuario, Long> {

    Optional<usuario> findByEmail(String email);

    Optional<usuario> findByUsername(String username);

    // ✅ Buscar usuário com a empresa carregada (JOIN FETCH)
    @Query("SELECT u FROM usuario u JOIN FETCH u.company WHERE u.email = :email")
    Optional<usuario> findByEmailWithCompany(@Param("email") String email);

    // lista todos os usuarios de uma empresa
    List<usuario> findByCompanyId(Long companyId);

    // Buscar usuario por ID e empresa
    Optional<usuario> findByIdAndCompanyId(Long id, Long companyId);

    // Buscar usuario por username e empresa
    Optional<usuario> findByUsernameAndCompanyId(String username, Long companyId);
}