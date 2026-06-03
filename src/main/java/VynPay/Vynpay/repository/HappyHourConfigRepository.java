package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.Company;
import VynPay.Vynpay.model.HappyHourConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface HappyHourConfigRepository extends JpaRepository<HappyHourConfig, Long> {

    // 🔥 MUDAR para List (pode ter mais de uma config ativa)
    List<HappyHourConfig> findByCompanyAndIsActiveTrue(Company company);

    // 🔥 MANTER este para compatibilidade (retorna o primeiro)
    default Optional<HappyHourConfig> findFirstByCompanyAndIsActiveTrue(Company company) {
        List<HappyHourConfig> configs = findByCompanyAndIsActiveTrue(company);
        return configs.isEmpty() ? Optional.empty() : Optional.of(configs.get(0));
    }

    Optional<HappyHourConfig> findByCompanyId(Long companyId);

    @Query("SELECT h FROM HappyHourConfig h WHERE h.company.id = :companyId AND h.isActive = true")
    List<HappyHourConfig> findActiveByCompanyId(@Param("companyId") Long companyId);
}