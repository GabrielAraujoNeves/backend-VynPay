package VynPay.Vynpay.repository;

import VynPay.Vynpay.enun.StatusComanda;
import VynPay.Vynpay.enun.TipoComanda;
import VynPay.Vynpay.model.Comanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ComandaRepository extends JpaRepository<Comanda, Long> {

    List<Comanda> findByCompanyId(Long companyId);

    List<Comanda> findByCompanyIdAndStatus(Long companyId, StatusComanda status);

    Optional<Comanda> findByNumeroComandaAndCompanyId(String numeroComanda, Long companyId);

    Optional<Comanda> findByClienteIdAndStatus(Long clienteId, StatusComanda status);

    // 🔥 CORRIGIDO: usar @Query explícita para evitar problemas com nomes de campos
    @Query("SELECT c FROM Comanda c WHERE c.identificadorComanda = :identificador AND c.tipoComanda = :tipo AND c.company.id = :companyId")
    Optional<Comanda> findByIdentificadorAndTipoAndCompanyId(
            @Param("identificador") String identificador,
            @Param("tipo") TipoComanda tipo,
            @Param("companyId") Long companyId
    );
}