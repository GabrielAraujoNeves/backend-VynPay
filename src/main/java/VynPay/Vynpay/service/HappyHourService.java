package VynPay.Vynpay.service;

import VynPay.Vynpay.dto.request.HappyHourConfigRequest;
import VynPay.Vynpay.dto.response.HappyHourConfigResponse;
import VynPay.Vynpay.dto.response.HappyHourProductResponse;
import VynPay.Vynpay.dto.response.ProdutoPromocaoResponse;
import VynPay.Vynpay.model.*;
import VynPay.Vynpay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class HappyHourService {

    @Autowired
    private HappyHourConfigRepository configRepository;

    @Autowired
    private HappyHourProductRepository happyHourProductRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    private boolean isWithinTimeRange(HappyHourConfig config) {
        if (config == null || !config.getIsActive()) {
            return false;
        }

        List<String> configuredDays = config.getDaysOfWeekList();
        if (configuredDays != null && !configuredDays.isEmpty()) {
            String today = LocalDate.now().getDayOfWeek().toString();
            if (!configuredDays.contains(today)) {
                return false;
            }
        }

        if (config.getStartTime() == null || config.getEndTime() == null) {
            return true;
        }

        LocalTime now = LocalTime.now();
        LocalTime start = config.getStartTime();
        LocalTime end = config.getEndTime();

        if (start.isBefore(end)) {
            return !now.isBefore(start) && !now.isAfter(end);
        } else {
            return !now.isBefore(start) || !now.isAfter(end);
        }
    }

    @Transactional
    public HappyHourConfigResponse createOrUpdateConfig(Company company, HappyHourConfigRequest request) {
        List<HappyHourConfig> existingConfigs = configRepository.findByCompanyAndIsActiveTrue(company);
        HappyHourConfig config;

        if (existingConfigs.isEmpty()) {
            config = new HappyHourConfig();
        } else {
            config = existingConfigs.getFirst();
        }

        config.setDiscountPercent(request.getDiscountPercent());
        config.setStartTime(request.getStartTime());
        config.setEndTime(request.getEndTime());
        config.setIsActive(true);
        config.setCompany(company);

        if (request.getDaysOfWeek() != null && !request.getDaysOfWeek().isEmpty()) {
            config.setDaysOfWeekList(request.getDaysOfWeek());
        } else {
            config.setDaysOfWeek(null);
        }

        config = configRepository.save(config);
        happyHourProductRepository.deleteByHappyHourConfig(config);

        List<Long> uniqueProductIds = request.getProductIds().stream().distinct().toList();

        for (Long productId : uniqueProductIds) {
            Produto product = produtoRepository.findByIdAndCompanyId(productId, company.getId())
                    .orElseThrow(() -> new RuntimeException("Produto nao encontrado: " + productId));

            HappyHourProduct happyHourProduct = new HappyHourProduct();
            happyHourProduct.setProduct(product);
            happyHourProduct.setHappyHourConfig(config);
            happyHourProduct.setIsActive(true);
            happyHourProductRepository.save(happyHourProduct);
        }

        return toResponse(config);
    }

    @Transactional
    public void deactivateHappyHour(Company company) {
        List<HappyHourConfig> activeConfigs = configRepository.findByCompanyAndIsActiveTrue(company);
        if (activeConfigs.isEmpty()) {
            throw new RuntimeException("Nenhuma configuracao ativa encontrada");
        }

        for (HappyHourConfig config : activeConfigs) {
            config.setIsActive(false);
            configRepository.save(config);
        }
    }

    public List<ProdutoPromocaoResponse> getProductsWithDiscount(Company company) {
        List<HappyHourConfig> activeConfigs = configRepository.findByCompanyAndIsActiveTrue(company);

        if (activeConfigs.isEmpty()) {
            List<Produto> produtosSemDesconto = produtoRepository.findByCompanyId(company.getId());
            return produtosSemDesconto.stream()
                    .map(produto -> new ProdutoPromocaoResponse(
                            produto.getId(),
                            produto.getNome(),
                            produto.getDescricao(),
                            produto.getPreco(),
                            produto.getPreco(),
                            0.0,
                            false
                    )).toList();
        }

        HappyHourConfig activeConfig = activeConfigs.getFirst();
        boolean isInTimeRange = isWithinTimeRange(activeConfig);

        List<Produto> produtos = produtoRepository.findByCompanyId(company.getId());

        return produtos.stream().map(produto -> {
            boolean isInHappyHour = false;
            BigDecimal precoPromocional = produto.getPreco();
            Double descontoPercent = 0.0;

            if (activeConfig.getIsActive() && isInTimeRange) {
                isInHappyHour = happyHourProductRepository.existsByHappyHourConfigAndProduct(activeConfig, produto);

                if (isInHappyHour) {
                    descontoPercent = activeConfig.getDiscountPercent();
                    BigDecimal desconto = produto.getPreco()
                            .multiply(BigDecimal.valueOf(descontoPercent / 100));
                    precoPromocional = produto.getPreco().subtract(desconto)
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }

            return new ProdutoPromocaoResponse(
                    produto.getId(),
                    produto.getNome(),
                    produto.getDescricao(),
                    produto.getPreco(),
                    precoPromocional,
                    isInHappyHour ? descontoPercent : 0.0,
                    isInHappyHour
            );
        }).toList();
    }

    public HappyHourConfigResponse getActiveConfig(Company company) {
        List<HappyHourConfig> activeConfigs = configRepository.findByCompanyAndIsActiveTrue(company);
        if (activeConfigs.isEmpty()) {
            return null;
        }
        return toResponse(activeConfigs.getFirst());
    }

    // 🔥 MÉTODO ADICIONADO - Verifica se um produto está no Happy Hour
    public boolean isProductInHappyHour(HappyHourConfigResponse config, Long productId) {
        if (config == null || config.getProducts() == null) {
            return false;
        }
        return config.getProducts().stream()
                .anyMatch(p -> p.getProductId().equals(productId));
    }

    private HappyHourConfigResponse toResponse(HappyHourConfig config) {
        List<HappyHourProductResponse> productResponses = config.getHappyHourProducts().stream()
                .filter(HappyHourProduct::getIsActive)
                .map(hp -> {
                    BigDecimal discountedPrice = hp.getProduct().getPreco()
                            .multiply(BigDecimal.valueOf(1 - config.getDiscountPercent() / 100))
                            .setScale(2, RoundingMode.HALF_UP);
                    return new HappyHourProductResponse(
                            hp.getProduct().getId(),
                            hp.getProduct().getNome(),
                            hp.getProduct().getPreco(),
                            discountedPrice
                    );
                }).toList();

        return new HappyHourConfigResponse(
                config.getId(),
                config.getIsActive(),
                config.getDiscountPercent(),
                config.getStartTime(),
                config.getEndTime(),
                config.getDaysOfWeekList(),
                productResponses
        );
    }

    public void forceActivateForTesting(Company company) {
        List<HappyHourConfig> configs = configRepository.findByCompanyAndIsActiveTrue(company);
        if (configs.isEmpty()) {
            throw new RuntimeException("Nenhuma configuração encontrada");
        }

        HappyHourConfig config = configs.getFirst();
        config.setStartTime(LocalTime.of(0, 0));
        config.setEndTime(LocalTime.of(23, 59));
        config.setIsActive(true);
        config.setDaysOfWeek("MON,TUE,WED,THU,FRI,SAT,SUN");
        configRepository.save(config);

        System.out.println("=== FORCE ACTIVATE ===");
        System.out.println("Config ID: " + config.getId());
        System.out.println("isActive: " + config.getIsActive());
        System.out.println("startTime: " + config.getStartTime());
        System.out.println("endTime: " + config.getEndTime());
        System.out.println("daysOfWeek: " + config.getDaysOfWeek());
    }
}
