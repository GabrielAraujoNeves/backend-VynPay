package VynPay.Vynpay.controller;

import VynPay.Vynpay.dto.request.HappyHourConfigRequest;
import VynPay.Vynpay.dto.response.HappyHourConfigResponse;
import VynPay.Vynpay.dto.response.ProdutoPromocaoResponse;
import VynPay.Vynpay.model.usuario;
import VynPay.Vynpay.service.HappyHourService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/happy-hour")
@CrossOrigin(origins = "*")
public class HappyHourController {

    @Autowired
    private HappyHourService happyHourService;

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

    @GetMapping("/products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProductsWithDiscount(@AuthenticationPrincipal usuario user) {
        try {
            List<ProdutoPromocaoResponse> produtos = happyHourService.getProductsWithDiscount(user.getCompany());

            Map<String, Object> response = new HashMap<>();
            response.put("total", produtos.size());
            response.put("produtos", produtos);
            response.put("isHappyHourActive", produtos.stream().anyMatch(ProdutoPromocaoResponse::getIsInHappyHour));

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
                response.put("message", "Nenhuma configuração ativa encontrada");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            return ResponseEntity.ok(config);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}