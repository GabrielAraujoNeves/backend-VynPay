package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    List<Pagamento> findByComandaId(Long comandaId);
}