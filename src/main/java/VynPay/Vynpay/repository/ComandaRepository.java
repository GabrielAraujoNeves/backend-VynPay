package VynPay.Vynpay.repository;

import VynPay.Vynpay.enun.StatusComanda;
import VynPay.Vynpay.enun.TipoComanda;
import VynPay.Vynpay.model.Comanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ComandaRepository extends JpaRepository<Comanda, Long> {

    List<Comanda> findByCompanyId(Long companyId);

    List<Comanda> findByCompanyIdAndStatus(Long companyId, StatusComanda status);

    Optional<Comanda> findByNumeroComandaAndCompanyId(String numeroComanda, Long companyId);

    Optional<Comanda> findByClienteIdAndStatus(Long clienteId, StatusComanda status);

    @Query("SELECT c FROM Comanda c WHERE c.identificadorComanda = :identificador AND c.tipoComanda = :tipo AND c.company.id = :companyId")
    List<Comanda> findByIdentificadorAndTipoAndCompanyId(
            @Param("identificador") String identificador,
            @Param("tipo") TipoComanda tipo,
            @Param("companyId") Long companyId
    );

    // ============================================
    // 🔥 MÉTODOS PARA RELATÓRIOS
    // ============================================

    // Buscar comandas pagas por período
    @Query("SELECT c FROM Comanda c WHERE c.status = 'PAGA' AND c.dataFechamento BETWEEN :inicio AND :fim AND c.company.id = :companyId")
    List<Comanda> findComandasPagasPorPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("companyId") Long companyId
    );

    // Buscar comandas pagas por dia
    @Query("SELECT c FROM Comanda c WHERE c.status = 'PAGA' AND DATE(c.dataFechamento) = :data AND c.company.id = :companyId")
    List<Comanda> findComandasPagasPorDia(
            @Param("data") LocalDate data,
            @Param("companyId") Long companyId
    );

    // Total vendas por período
    @Query("SELECT COALESCE(SUM(c.valorTotal), 0) FROM Comanda c WHERE c.status = 'PAGA' AND c.dataFechamento BETWEEN :inicio AND :fim AND c.company.id = :companyId")
    BigDecimal sumVendasPorPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim,
            @Param("companyId") Long companyId
    );
}