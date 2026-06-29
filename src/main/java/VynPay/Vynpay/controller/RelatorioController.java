package VynPay.Vynpay.controller;

import VynPay.Vynpay.dto.response.RelatorioPerdidosDTO;
import VynPay.Vynpay.dto.response.RelatorioVendasResponseDTO;
import VynPay.Vynpay.model.usuario;
import VynPay.Vynpay.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/relatorios")
@CrossOrigin(origins = "*")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    // ============================================
    // VENDAS POR PERÍODO
    // ============================================

    @GetMapping("/vendas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getVendasPorPeriodo(
            @AuthenticationPrincipal usuario admin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        try {
            RelatorioVendasResponseDTO relatorio = relatorioService.getVendasPorPeriodo(
                    inicio, fim, admin.getCompany().getId()
            );
            return ResponseEntity.ok(relatorio);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================
    // ITENS PERDIDOS / CANCELADOS
    // ============================================

    @GetMapping("/perdidos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getItensPerdidos(
            @AuthenticationPrincipal usuario admin,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        try {
            RelatorioPerdidosDTO relatorio = relatorioService.getItensPerdidos(
                    inicio, fim, admin.getCompany().getId()
            );
            return ResponseEntity.ok(relatorio);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================
    // DASHBOARD RESUMIDO
    // ============================================

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal usuario admin) {
        try {
            Map<String, Object> dashboard = relatorioService.getDashboard(admin.getCompany().getId());
            return ResponseEntity.ok(dashboard);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}