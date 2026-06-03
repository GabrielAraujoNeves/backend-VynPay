package VynPay.Vynpay.controller;

import VynPay.Vynpay.dto.request.HappyHourConfigRequest;
import VynPay.Vynpay.dto.response.HappyHourConfigResponse;
import VynPay.Vynpay.dto.response.ProdutoPromocaoResponse;
import VynPay.Vynpay.model.HappyHourConfig;
import VynPay.Vynpay.model.HappyHourProduct;
import VynPay.Vynpay.model.Produto;
import VynPay.Vynpay.model.usuario;
import VynPay.Vynpay.repository.HappyHourConfigRepository;
import VynPay.Vynpay.repository.HappyHourProductRepository;
import VynPay.Vynpay.repository.ProdutoRepository;
import VynPay.Vynpay.service.HappyHourService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/happy-hour")
@CrossOrigin(origins = "*")
public class HappyHourController {

    @Autowired
    private HappyHourService happyHourService;

    @Autowired
    private HappyHourConfigRepository configRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private HappyHourProductRepository happyHourProductRepository;

    @PostMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> configureHappyHour(
            @AuthenticationPrincipal usuario admin,
            @Valid @RequestBody HappyHourConfigRequest request
    ) {
        try {
            HappyHourConfigResponse response = happyHourService.createOrUpdateConfig(
                    admin.getCompany(),
                    request
            );

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Happy Hour configurado com sucesso!");
            result.put("config", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateHappyHour(@AuthenticationPrincipal usuario admin) {
        try {
            happyHourService.deactivateHappyHour(admin.getCompany());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Happy Hour desativado com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // 🔥 CORRIGIDO: Agora usa a mesma lógica do ProdutoController
    @GetMapping("/products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProductsWithDiscount(@AuthenticationPrincipal usuario user) {
        try {
            List<Produto> produtos = produtoRepository.findByCompanyId(user.getCompany().getId());
            HappyHourConfigResponse activeConfig = happyHourService.getActiveConfig(user.getCompany());

            // Verificar se Happy Hour está ativo
            boolean isHappyHourActive = false;
            Double descontoGlobal = 0.0;

            if (activeConfig != null && activeConfig.getIsActive()) {
                isHappyHourActive = isWithinTimeRange(activeConfig);
                if (isHappyHourActive) {
                    descontoGlobal = activeConfig.getDiscountPercent();
                }
            }

            final Double descontoFinal = descontoGlobal;
            final boolean happyHourAtivo = isHappyHourActive;
            final HappyHourConfigResponse configFinal = activeConfig;

            List<Map<String, Object>> produtosResponse = produtos.stream().map(produto -> {
                Map<String, Object> produtoMap = new HashMap<>();
                produtoMap.put("id", produto.getId());
                produtoMap.put("nome", produto.getNome());
                produtoMap.put("descricao", produto.getDescricao());

                boolean produtoEmPromocao = false;
                BigDecimal precoPromocional = produto.getPreco();
                Double descontoAplicado = 0.0;

                if (happyHourAtivo && configFinal != null) {
                    produtoEmPromocao = happyHourService.isProductInHappyHour(configFinal, produto.getId());
                    if (produtoEmPromocao) {
                        descontoAplicado = descontoFinal;
                        BigDecimal desconto = produto.getPreco()
                                .multiply(BigDecimal.valueOf(descontoAplicado / 100));
                        precoPromocional = produto.getPreco().subtract(desconto)
                                .setScale(2, RoundingMode.HALF_UP);
                    }
                }

                produtoMap.put("precoOriginal", produto.getPreco());
                produtoMap.put("precoPromocional", precoPromocional);
                produtoMap.put("descontoPercent", produtoEmPromocao ? descontoAplicado : 0.0);
                produtoMap.put("isInHappyHour", produtoEmPromocao);

                return produtoMap;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", produtosResponse.size());
            response.put("produtos", produtosResponse);
            response.put("isHappyHourActive", happyHourAtivo);

            if (happyHourAtivo) {
                response.put("happyHourMessage", " HAPPY HOUR ATIVO! " + descontoGlobal + "% de desconto! ");
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/config/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getActiveConfig(@AuthenticationPrincipal usuario admin) {
        try {
            HappyHourConfigResponse config = happyHourService.getActiveConfig(admin.getCompany());

            if (config == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Nenhuma configuracao ativa encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            return ResponseEntity.ok(config);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/test/force-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> forceActivate(@AuthenticationPrincipal usuario admin) {
        try {
            List<HappyHourConfig> activeConfigs = configRepository.findByCompanyAndIsActiveTrue(admin.getCompany());
            HappyHourConfig config;

            if (activeConfigs.isEmpty()) {
                config = new HappyHourConfig();
                config.setCompany(admin.getCompany());
                config.setDiscountPercent(50.0);
                config.setStartTime(LocalTime.of(0, 0));
                config.setEndTime(LocalTime.of(23, 59));
                config.setIsActive(true);
                config = configRepository.save(config);
            } else {
                config = activeConfigs.getFirst();
                config.setStartTime(LocalTime.of(0, 0));
                config.setEndTime(LocalTime.of(23, 59));
                config.setIsActive(true);
                config = configRepository.save(config);
            }

            List<Produto> produtos = produtoRepository.findByCompanyId(admin.getCompany().getId());
            happyHourProductRepository.deleteByHappyHourConfig(config);

            for (Produto produto : produtos) {
                HappyHourProduct hp = new HappyHourProduct();
                hp.setProduct(produto);
                hp.setHappyHourConfig(config);
                hp.setIsActive(true);
                happyHourProductRepository.save(hp);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Happy Hour forcado com sucesso para " + produtos.size() + " produtos");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/debug/check")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> debugCheck(@AuthenticationPrincipal usuario admin) {
        Map<String, Object> response = new HashMap<>();

        List<HappyHourConfig> configs = configRepository.findByCompanyAndIsActiveTrue(admin.getCompany());
        response.put("configsFound", configs.size());

        if (!configs.isEmpty()) {
            HappyHourConfig config = configs.getFirst();
            response.put("configId", config.getId());
            response.put("isActive", config.getIsActive());
            response.put("startTime", config.getStartTime());
            response.put("endTime", config.getEndTime());
            response.put("daysOfWeek", config.getDaysOfWeek());
            response.put("currentTime", LocalTime.now().toString());
            response.put("currentDay", LocalDate.now().getDayOfWeek().toString());

            List<String> configuredDays = config.getDaysOfWeekList();
            boolean dayCheck = configuredDays.isEmpty();
            if (!configuredDays.isEmpty()) {
                String today = LocalDate.now().getDayOfWeek().toString();
                dayCheck = configuredDays.contains(today);
            }

            boolean timeCheck = true;
            if (config.getStartTime() != null && config.getEndTime() != null) {
                LocalTime now = LocalTime.now();
                LocalTime start = config.getStartTime();
                LocalTime end = config.getEndTime();
                if (start.isBefore(end)) {
                    timeCheck = !now.isBefore(start) && !now.isAfter(end);
                } else {
                    timeCheck = !now.isBefore(start) || !now.isAfter(end);
                }
            }

            response.put("dayCheck", dayCheck);
            response.put("timeCheck", timeCheck);
            response.put("isActiveCheck", config.getIsActive());
            response.put("finalResult", config.getIsActive() && dayCheck && timeCheck);

            List<HappyHourProduct> products = happyHourProductRepository.findByHappyHourConfig(config);
            response.put("productsLinkedCount", products.size());
            response.put("productIds", products.stream().map(p -> p.getProduct().getId()).distinct().toList());
        }

        return ResponseEntity.ok(response);
    }

    private boolean isWithinTimeRange(HappyHourConfigResponse config) {
        if (config == null || !config.getIsActive()) {
            return false;
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
}