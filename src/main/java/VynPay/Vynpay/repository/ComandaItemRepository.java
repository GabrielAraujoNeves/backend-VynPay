package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.ComandaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComandaItemRepository extends JpaRepository<ComandaItem, Long> {
    List<ComandaItem> findByComandaId(Long comandaId);

    //MÉTODO PARA PRODUTOS MAIS VENDIDOS
    @Query("SELECT ci.produto.id as produtoId, p.nome as nomeProduto, " +
            "SUM(ci.quantidade) as quantidadeVendida, SUM(ci.precoTotal) as valorTotal " +
            "FROM ComandaItem ci JOIN ci.produto p " +
            "WHERE ci.comanda.id IN :comandaIds " +
            "GROUP BY ci.produto.id, p.nome " +
            "ORDER BY quantidadeVendida DESC")
    List<Object[]> findProdutosMaisVendidos(@Param("comandaIds") List<Long> comandaIds);
    void deleteByComandaId(Long comandaId);
}