package VynPay.Vynpay.controller;

import VynPay.Vynpay.dto.request.*;
import VynPay.Vynpay.enun.StatusComanda;
import VynPay.Vynpay.model.*;
import VynPay.Vynpay.service.ComandaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comanda")
@CrossOrigin(origins = "*")
public class ComandaController {

    @Autowired
    private ComandaService comandaService;

    // ========== CLIENTES ==========

    @PostMapping("/clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cadastrarCliente(
            @AuthenticationPrincipal usuario admin,
            @RequestBody ClienteRequest request
    ) {
        try {
            Cliente cliente = comandaService.cadastrarCliente(
                    admin.getCompany(),
                    request.getNome(),
                    request.getCpf(),
                    request.getTelefone()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cliente cadastrado com sucesso");
            response.put("id", cliente.getId());
            response.put("nome", cliente.getNome());
            response.put("cpf", cliente.getCpf());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/clientes")
    public ResponseEntity<?> listarClientes(@AuthenticationPrincipal usuario admin) {
        try {
            List<Cliente> clientes = comandaService.listarClientes(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("total", clientes.size());
            response.put("clientes", clientes);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/clientes/comanda-ativa")
    public ResponseEntity<?> listarClientesComComandaAtiva(@AuthenticationPrincipal usuario admin) {
        try {
            List<Cliente> clientes = comandaService.listarClientesComComandaAtiva(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("total", clientes.size());
            response.put("clientes", clientes);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== COMANDAS ==========

    @PostMapping("/abrir")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> abrirComanda(
            @AuthenticationPrincipal usuario admin,
            @RequestParam Long clienteId
    ) {
        try {
            Comanda comanda = comandaService.abrirComanda(admin.getCompany(), clienteId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comanda aberta com sucesso");
            response.put("numeroComanda", comanda.getNumeroComanda());
            response.put("cliente", comanda.getCliente().getNome());
            response.put("dataAbertura", comanda.getDataAbertura());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/{comandaId}/adicionar-produto")
    public ResponseEntity<?> adicionarProduto(
            @AuthenticationPrincipal usuario user,
            @PathVariable Long comandaId,
            @RequestBody AdicionarProdutoRequest request
    ) {
        try {
            ComandaItem item = comandaService.adicionarProdutoNaComanda(
                    comandaId,
                    request.getProdutoId(),
                    request.getQuantidade(),
                    user.getCompany().getId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Produto adicionado com sucesso");
            response.put("produto", item.getProduto().getNome());
            response.put("quantidade", item.getQuantidade());
            response.put("precoTotal", item.getPrecoTotal());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/{comandaId}/itens")
    public ResponseEntity<?> listarItensComanda(
            @AuthenticationPrincipal usuario user,
            @PathVariable Long comandaId
    ) {
        try {
            List<ComandaItem> itens = comandaService.listarItensDaComanda(comandaId, user.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("total", itens.size());
            response.put("itens", itens);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/itens/{itemId}")
    public ResponseEntity<?> removerItem(
            @AuthenticationPrincipal usuario user,
            @PathVariable Long itemId
    ) {
        try {
            comandaService.removerItemDaComanda(itemId, user.getCompany().getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Item removido com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== FECHAMENTO E PAGAMENTO ==========

    @PostMapping("/{comandaId}/fechar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> fecharComanda(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long comandaId
    ) {
        try {
            Comanda comanda = comandaService.fecharComanda(comandaId, admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comanda fechada com sucesso");
            response.put("numeroComanda", comanda.getNumeroComanda());
            response.put("valorTotal", comanda.getValorTotal());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/{comandaId}/pagar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> realizarPagamento(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long comandaId,
            @RequestBody PagamentoRequest request
    ) {
        try {
            Pagamento pagamento = comandaService.realizarPagamento(
                    comandaId,
                    request.getValorPago(),
                    request.getFormaPagamento(),
                    admin.getCompany().getId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pagamento realizado com sucesso");
            response.put("valorPago", pagamento.getValorPago());
            response.put("formaPagamento", pagamento.getFormaPagamento());
            response.put("dataPagamento", pagamento.getDataPagamento());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/relatorio/dia")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> relatorioDoDia(@AuthenticationPrincipal usuario admin) {
        try {
            BigDecimal total = comandaService.calcularTotalDoDia(admin.getCompany().getId());
            List<Comanda> comandasPagas = comandaService.listarComandasPorStatus(admin.getCompany().getId(), StatusComanda.PAGA);

            Map<String, Object> response = new HashMap<>();
            response.put("data", java.time.LocalDate.now());
            response.put("totalVendas", total);
            response.put("quantidadeComandas", comandasPagas.size());
            response.put("comandas", comandasPagas);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}