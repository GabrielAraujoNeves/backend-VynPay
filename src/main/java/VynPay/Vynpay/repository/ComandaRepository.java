package VynPay.Vynpay.repository;

import VynPay.Vynpay.enun.StatusComanda;
import VynPay.Vynpay.model.Comanda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComandaRepository extends JpaRepository<Comanda, Long> {
    List<Comanda> findByCompanyId(Long companyId);
    List<Comanda> findByCompanyIdAndStatus(Long companyId, StatusComanda status);
    Optional<Comanda> findByNumeroComandaAndCompanyId(String numeroComanda, Long companyId);
    Optional<Comanda> findByClienteIdAndStatus(Long clienteId, StatusComanda status);
}
