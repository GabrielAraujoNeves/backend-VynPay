package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.Pulseira;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PulseiraRepository extends JpaRepository<Pulseira, Long> {
    List<Pulseira> findByCompanyId(Long companyId);
    Optional<Pulseira> findByNumeroPulseiraAndCompanyId(String numeroPulseira, Long companyId);
    List<Pulseira> findByCompanyIdAndIsAtivoTrue(Long companyId);
    List<Pulseira> findByPulseiraAgrupadaCom(String pulseiraAgrupadaCom);
}