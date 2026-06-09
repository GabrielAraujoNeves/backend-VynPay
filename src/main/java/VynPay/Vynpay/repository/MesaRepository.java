package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    List<Mesa> findByCompanyId(Long companyId);
    Optional<Mesa> findByNumeroMesaAndCompanyId(Integer numeroMesa, Long companyId);
    List<Mesa> findByCompanyIdAndIsOcupadaTrue(Long companyId);
}