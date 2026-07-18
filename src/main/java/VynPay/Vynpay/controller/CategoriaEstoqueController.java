package VynPay.Vynpay.controller;

import VynPay.Vynpay.dto.request.CategoriaEstoqueRequest;
import VynPay.Vynpay.dto.request.CategoriaUpdateRequest;
import VynPay.Vynpay.dto.response.CategoriaEstoqueResponse;
import VynPay.Vynpay.model.usuario;
import VynPay.Vynpay.service.CategoriaService;
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
@RequestMapping("/categoria/estoque")
@CrossOrigin(origins = "*")
public class CategoriaEstoqueController {

    @Autowired
    private CategoriaService categoriaService;

    // ========== CRIAR ==========
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarCategoriaEstoque(
            @AuthenticationPrincipal usuario admin,
            @RequestBody CategoriaEstoqueRequest request
    ) {
        try {
            CategoriaEstoqueResponse response = categoriaService.criarCategoriaEstoque(admin.getCompany(), request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Categoria de estoque criada com sucesso");
            result.put("id", response.getId());
            result.put("nome", response.getNome());

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== LISTAR TODAS ==========
    @GetMapping
    public ResponseEntity<?> listarCategoriasEstoque(@AuthenticationPrincipal usuario admin) {
        try {
            List<CategoriaEstoqueResponse> categorias = categoriaService.listarCategoriasEstoque(
                    admin.getCompany().getId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("total", categorias.size());
            response.put("categorias", categorias);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== LISTAR ATIVAS ==========
    @GetMapping("/ativas")
    public ResponseEntity<?> listarCategoriasEstoqueAtivas(@AuthenticationPrincipal usuario admin) {
        try {
            List<CategoriaEstoqueResponse> categorias = categoriaService.listarCategoriasEstoqueAtivas(
                    admin.getCompany().getId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("total", categorias.size());
            response.put("categorias", categorias);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== BUSCAR POR NOME ==========
    @GetMapping("/search")
    public ResponseEntity<?> buscarCategoriasEstoquePorNome(
            @AuthenticationPrincipal usuario admin,
            @RequestParam String nome
    ) {
        try {
            List<CategoriaEstoqueResponse> categorias = categoriaService.buscarCategoriasEstoquePorNome(
                    admin.getCompany().getId(), nome
            );

            Map<String, Object> response = new HashMap<>();
            response.put("total", categorias.size());
            response.put("categorias", categorias);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== EDITAR ==========
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editarCategoriaEstoque(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long id,
            @RequestBody CategoriaUpdateRequest request
    ) {
        try {
            CategoriaEstoqueResponse response = categoriaService.editarCategoriaEstoque(
                    id, admin.getCompany().getId(), request
            );

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Categoria de estoque atualizada com sucesso");
            result.put("id", response.getId());
            result.put("nome", response.getNome());

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== DELETAR ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarCategoriaEstoque(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long id
    ) {
        try {
            categoriaService.deletarCategoriaEstoque(id, admin.getCompany().getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Categoria de estoque deletada com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}