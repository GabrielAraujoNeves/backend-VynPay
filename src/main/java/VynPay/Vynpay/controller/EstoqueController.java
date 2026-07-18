package VynPay.Vynpay.controller;

import VynPay.Vynpay.dto.request.EstoqueRequest;
import VynPay.Vynpay.dto.response.EstoqueResponse;
import VynPay.Vynpay.model.Estoque;
import VynPay.Vynpay.model.usuario;
import VynPay.Vynpay.service.EstoqueService;
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
@RequestMapping("/estoque")
@CrossOrigin(origins = "*")
public class EstoqueController {

    @Autowired
    private EstoqueService estoqueService;

    // ========== CRUD BÁSICO ==========

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarItemEstoque(
            @AuthenticationPrincipal usuario admin,
            @RequestBody EstoqueRequest request
    ) {
        try {
            Estoque item = estoqueService.criarItemEstoque(admin.getCompany(), request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item adicionado ao estoque com sucesso");
            response.put("id", item.getId());
            response.put("nomeProduto", item.getNomeProduto());
            response.put("quantidade", item.getQuantidade());
            response.put("unidadeMedida", item.getUnidadeMedida());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> listarEstoque(@AuthenticationPrincipal usuario admin) {
        try {
            List<EstoqueResponse> estoque = estoqueService.listarEstoque(admin.getCompany().getId());
            Double valorTotal = estoqueService.getValorTotalEstoque(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("totalItens", estoque.size());
            response.put("valorTotalEstoque", valorTotal != null ? valorTotal : 0.0);
            response.put("itens", estoque);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long id
    ) {
        try {
            EstoqueResponse item = estoqueService.buscarPorId(id, admin.getCompany().getId());
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarItem(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long id,
            @RequestBody EstoqueRequest request
    ) {
        try {
            Estoque item = estoqueService.atualizarItem(id, admin.getCompany().getId(), request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item atualizado com sucesso");
            response.put("id", item.getId());
            response.put("nomeProduto", item.getNomeProduto());
            response.put("quantidade", item.getQuantidade());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarItem(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long id
    ) {
        try {
            estoqueService.deletarItem(id, admin.getCompany().getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Item removido do estoque com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== OPERAÇÕES DE ESTOQUE ==========

    @PostMapping("/{id}/adicionar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adicionarQuantidade(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long id,
            @RequestParam Integer quantidade
    ) {
        try {
            Estoque item = estoqueService.adicionarQuantidade(id, admin.getCompany().getId(), quantidade);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quantidade adicionada com sucesso");
            response.put("id", item.getId());
            response.put("nomeProduto", item.getNomeProduto());
            response.put("quantidadeAtual", item.getQuantidade());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/{id}/remover")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removerQuantidade(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long id,
            @RequestParam Integer quantidade
    ) {
        try {
            Estoque item = estoqueService.removerQuantidade(id, admin.getCompany().getId(), quantidade);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quantidade removida com sucesso");
            response.put("id", item.getId());
            response.put("nomeProduto", item.getNomeProduto());
            response.put("quantidadeAtual", item.getQuantidade());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== CONSULTAS ESPECIALIZADAS ==========

    @GetMapping("/search")
    public ResponseEntity<?> buscarPorNome(
            @AuthenticationPrincipal usuario admin,
            @RequestParam String nome
    ) {
        try {
            List<EstoqueResponse> itens = estoqueService.buscarPorNome(admin.getCompany().getId(), nome);

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

    // CORRIGIDO: Agora recebe Long (ID da categoria) ao invés de String
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<?> buscarPorCategoria(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long categoriaId
    ) {
        try {
            List<EstoqueResponse> itens = estoqueService.buscarPorCategoria(admin.getCompany().getId(), categoriaId);

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

    @GetMapping("/estoque-baixo")
    public ResponseEntity<?> estoqueBaixo(@AuthenticationPrincipal usuario admin) {
        try {
            List<EstoqueResponse> itens = estoqueService.buscarEstoqueBaixo(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("total", itens.size());
            response.put("mensagem", itens.isEmpty() ? "Nenhum item com estoque baixo" : "Itens com estoque abaixo do mínimo");
            response.put("itens", itens);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/vencidos")
    public ResponseEntity<?> produtosVencidos(@AuthenticationPrincipal usuario admin) {
        try {
            List<EstoqueResponse> itens = estoqueService.buscarVencidos(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("total", itens.size());
            response.put("mensagem", itens.isEmpty() ? "Nenhum produto vencido" : "Produtos vencidos encontrados");
            response.put("itens", itens);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/proximos-vencer")
    public ResponseEntity<?> proximosVencer(@AuthenticationPrincipal usuario admin) {
        try {
            List<EstoqueResponse> itens = estoqueService.buscarProximosVencer(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("total", itens.size());
            response.put("mensagem", itens.isEmpty() ? "Nenhum produto próximo a vencer" : "Produtos que vencem nos próximos 30 dias");
            response.put("itens", itens);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/fornecedor")
    public ResponseEntity<?> buscarPorFornecedor(
            @AuthenticationPrincipal usuario admin,
            @RequestParam String fornecedor
    ) {
        try {
            List<EstoqueResponse> itens = estoqueService.buscarPorFornecedor(admin.getCompany().getId(), fornecedor);

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

    @GetMapping("/resumo-categorias")
    public ResponseEntity<?> resumoPorCategoria(@AuthenticationPrincipal usuario admin) {
        try {
            List<Object[]> resumo = estoqueService.getResumoPorCategoria(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("resumo", resumo);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/valor-total")
    public ResponseEntity<?> valorTotalEstoque(@AuthenticationPrincipal usuario admin) {
        try {
            Double valorTotal = estoqueService.getValorTotalEstoque(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("valorTotalEstoque", valorTotal != null ? valorTotal : 0.0);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}