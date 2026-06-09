package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.ClienteComanda;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteComandaRepository extends JpaRepository<ClienteComanda, Long> {
    List<ClienteComanda> findByComandaId(Long comandaId);
}