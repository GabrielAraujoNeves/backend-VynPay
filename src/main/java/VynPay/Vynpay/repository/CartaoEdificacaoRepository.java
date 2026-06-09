package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.CartaoEdificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartaoEdificacaoRepository extends JpaRepository<CartaoEdificacao, Long> {
    List<CartaoEdificacao> findByCompanyId(Long companyId);
    Optional<CartaoEdificacao> findByNumeroCartaoAndCompanyId(String numeroCartao, Long companyId);
    List<CartaoEdificacao> findByCompanyIdAndIsAtivoTrue(Long companyId);
    List<CartaoEdificacao> findByCartaoVinculado(String cartaoVinculado);
}