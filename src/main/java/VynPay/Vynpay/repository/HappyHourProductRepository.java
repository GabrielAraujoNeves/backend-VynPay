package VynPay.Vynpay.repository;

import VynPay.Vynpay.model.HappyHourConfig;
import VynPay.Vynpay.model.HappyHourProduct;
import VynPay.Vynpay.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HappyHourProductRepository extends JpaRepository<HappyHourProduct, Long> {
    List<HappyHourProduct> findByHappyHourConfig(HappyHourConfig config);
    void deleteByHappyHourConfig(HappyHourConfig config);
    boolean existsByHappyHourConfigAndProduct(HappyHourConfig config, Produto product);
}