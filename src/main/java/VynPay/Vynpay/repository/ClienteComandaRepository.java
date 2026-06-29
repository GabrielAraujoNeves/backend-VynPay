package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.ClienteComanda;
import VynPay.Vynpay.model.Comanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteComandaRepository extends JpaRepository<ClienteComanda, Long> {
    List<ClienteComanda> findByComandaId(Long comandaId);

    // 🔥 ADICIONAR ESTE MÉTODO
    List<ClienteComanda> findByComandaIn(List<Comanda> comandas);

    // 🔥 OU USAR QUERY (alternativa)
    @Query("SELECT c FROM ClienteComanda c WHERE c.comanda IN :comandas")
    List<ClienteComanda> findClientesByComandas(@Param("comandas") List<Comanda> comandas);
}