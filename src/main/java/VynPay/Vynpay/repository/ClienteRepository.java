package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository  extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCpf(String cpf);
    List<Cliente> findByCompanyId(Long companyId);
    Optional<Cliente> findByIdAndCompanyId(Long id, Long componyId);
    List<Cliente> findByCompanyIdAndComandaAtivaTrue(Long companyId);
}
