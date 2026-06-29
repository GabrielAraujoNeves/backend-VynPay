package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.LogRemocaoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LogRemocaoItemRepository extends JpaRepository<LogRemocaoItem, Long> {

    @Query("SELECT l FROM LogRemocaoItem l WHERE l.dataRemocao BETWEEN :inicio AND :fim AND l.companyId = :companyId")
    List<LogRemocaoItem> findByDataRemocaoBetweenAndCompanyId(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("companyId") Long companyId
    );

    @Query("SELECT l FROM LogRemocaoItem l WHERE DATE(l.dataRemocao) = :data AND l.companyId = :companyId")
    List<LogRemocaoItem> findByDataRemocaoAndCompanyId(
            @Param("data") LocalDateTime data,
            @Param("companyId") Long companyId
    );
}