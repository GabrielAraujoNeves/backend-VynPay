package VynPay.Vynpay.controller;

import VynPay.Vynpay.dto.request.CategoriaProdutoRequest;
import VynPay.Vynpay.dto.request.CategoriaUpdateRequest;
import VynPay.Vynpay.dto.response.CategoriaProdutoResponse;
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
@RequestMapping("/categoria/produtos")
@CrossOrigin(origins = "*")
public class CategoriaProdutoController {

    @Autowired
    private CategoriaService categoriaService;

    // ========== CRIAR ==========
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarCategoriaProduto(
            @AuthenticationPrincipal usuario admin,
            @RequestBody CategoriaProdutoRequest request
    ) {
        try {
            CategoriaProdutoResponse response = categoriaService.criarCategoriaProduto(admin.getCompany(), request);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Categoria de produto criada com sucesso");
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
    public ResponseEntity<?> listarCategoriasProduto(@AuthenticationPrincipal usuario admin) {
        try {
            List<CategoriaProdutoResponse> categorias = categoriaService.listarCategoriasProduto(
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
    public ResponseEntity<?> listarCategoriasProdutoAtivas(@AuthenticationPrincipal usuario admin) {
        try {
            List<CategoriaProdutoResponse> categorias = categoriaService.listarCategoriasProdutoAtivas(
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
    public ResponseEntity<?> buscarCategoriasProdutoPorNome(
            @AuthenticationPrincipal usuario admin,
            @RequestParam String nome
    ) {
        try {
            List<CategoriaProdutoResponse> categorias = categoriaService.buscarCategoriasProdutoPorNome(
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
    public ResponseEntity<?> editarCategoriaProduto(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long id,
            @RequestBody CategoriaUpdateRequest request
    ) {
        try {
            CategoriaProdutoResponse response = categoriaService.editarCategoriaProduto(
                    id, admin.getCompany().getId(), request
            );

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Categoria de produto atualizada com sucesso");
            result.put("id", response.getId());
            result.put("nome", response.getNome());

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== DELETAR (AGORA ACEITA NULL) ==========
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarCategoriaProduto(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long id
    ) {
        try {
            categoriaService.deletarCategoriaProduto(id, admin.getCompany().getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Categoria de produto deletada com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== MÉTODO GENÉRICO PARA DELETAR QUALQUER CATEGORIA ==========
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarCategoria(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long id
    ) {
        try {
            categoriaService.deletarCategoria(id, admin.getCompany().getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Categoria deletada com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}