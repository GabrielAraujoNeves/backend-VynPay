package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.ComandaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComandaItemRepository extends JpaRepository<ComandaItem, Long> {
    List<ComandaItem> findByComandaId(Long comandaId);
}