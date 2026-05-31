package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.Company;
import VynPay.Vynpay.model.HappyHourConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HappyHourConfigRepository extends JpaRepository<HappyHourConfig, Long> {
    Optional<HappyHourConfig> findByCompanyAndIsActiveTrue(Company company);
    Optional<HappyHourConfig> findByCompanyId(Long companyId);
}