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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HappyHourService {

    @Autowired
    private HappyHourConfigRepository configRepository;

    @Autowired
    private HappyHourProductRepository happyHourProductRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CompanyRepository companyRepository;

    private boolean isWithinTimeRange(HappyHourConfig config) {
        if (config == null || config.getStartTime() == null || config.getEndTime() == null) {
            return true;
        }
        LocalTime now = LocalTime.now();
        return !now.isBefore(config.getStartTime()) && !now.isAfter(config.getEndTime());
    }

    @Transactional
    public HappyHourConfigResponse createOrUpdateConfig(Company company, HappyHourConfigRequest request) {
        // Buscar configuração existente ou criar nova
        HappyHourConfig config = configRepository.findByCompanyAndIsActiveTrue(company)
                .orElse(new HappyHourConfig());

        config.setDiscountPercent(request.getDiscountPercent());
        config.setStartTime(request.getStartTime());
        config.setEndTime(request.getEndTime());
        config.setIsActive(true);
        config.setCompany(company);

        config = configRepository.save(config);

        // Limpar produtos antigos
        happyHourProductRepository.deleteByHappyHourConfig(config);

        // Adicionar novos produtos
        for (Long productId : request.getProductIds()) {
            Produto product = produtoRepository.findByIdAndCompanyId(productId, company.getId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + productId));

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
        HappyHourConfig config = configRepository.findByCompanyAndIsActiveTrue(company)
                .orElseThrow(() -> new RuntimeException("Nenhuma configuração ativa encontrada"));
        config.setIsActive(false);
        configRepository.save(config);
    }

    public List<ProdutoPromocaoResponse> getProductsWithDiscount(Company company) {
        HappyHourConfig activeConfig = configRepository.findByCompanyAndIsActiveTrue(company).orElse(null);
        boolean isInTimeRange = isWithinTimeRange(activeConfig);

        List<Produto> produtos = produtoRepository.findByCompanyId(company.getId());

        return produtos.stream().map(produto -> {
            boolean isInHappyHour = false;
            BigDecimal precoPromocional = produto.getPreco();
            Double descontoPercent = 0.0;

            if (activeConfig != null && activeConfig.getIsActive() && isInTimeRange) {
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
        }).collect(Collectors.toList());
    }

    public HappyHourConfigResponse getActiveConfig(Company company) {
        HappyHourConfig config = configRepository.findByCompanyAndIsActiveTrue(company).orElse(null);
        if (config == null) {
            return null;
        }
        return toResponse(config);
    }

    private HappyHourConfigResponse toResponse(HappyHourConfig config) {
        List<HappyHourProductResponse> productResponses = config.getHappyHourProducts().stream()
                .filter(hp -> hp.getIsActive())
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
                }).collect(Collectors.toList());

        return new HappyHourConfigResponse(
                config.getId(),
                config.getIsActive(),
                config.getDiscountPercent(),
                config.getStartTime(),
                config.getEndTime(),
                productResponses
        );
    }
}