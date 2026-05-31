package VynPay.Vynpay.controller;

import VynPay.Vynpay.dto.request.CategoriaRequest;
import VynPay.Vynpay.dto.request.ProdutoRequest;
import VynPay.Vynpay.dto.response.ProdutoResponse;
import VynPay.Vynpay.dto.response.CategoriaResponse;
import VynPay.Vynpay.model.CategoriaProduto;
import VynPay.Vynpay.model.Produto;
import VynPay.Vynpay.model.usuario;
import VynPay.Vynpay.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/produtos")
@CrossOrigin(origins = "*")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    // ========== CATEGORIAS ==========

    @PostMapping("/categorias")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarCategoria(
            @AuthenticationPrincipal usuario admin,
            @RequestBody CategoriaRequest request
    ) {
        try {
            CategoriaProduto categoria = produtoService.criarCategoria(
                    admin.getCompany(),
                    request.getNome(),
                    request.getDescricao()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Categoria criada com sucesso");
            response.put("id", categoria.getId());
            response.put("nome", categoria.getNome());
            response.put("descricao", categoria.getDescricao());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Listar categorias (sem dados da empresa)
    @GetMapping("/categorias")
    public ResponseEntity<?> listarCategorias(@AuthenticationPrincipal usuario admin) {
        try {
            List<CategoriaProduto> categorias = produtoService.listarCategorias(admin.getCompany().getId());

            // Converter para resposta customizada sem empresa
            List<CategoriaResponse> categoriasResponse = categorias.stream().map(cat -> {
                CategoriaResponse response = new CategoriaResponse();
                response.setId(cat.getId());
                response.setNome(cat.getNome());
                response.setDescricao(cat.getDescricao());
                response.setCreatedAt(cat.getCreatedAt());
                response.setUpdatedAt(cat.getUpdatedAt());
                return response;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", categoriasResponse.size());
            response.put("categorias", categoriasResponse);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Buscar categoria por ID (sem dados da empresa)
    @GetMapping("/categorias/{categoriaId}")
    public ResponseEntity<?> buscarCategoriaPorId(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long categoriaId
    ) {
        try {
            CategoriaProduto categoria = produtoService.buscarCategoriaPorId(categoriaId, admin.getCompany().getId());

            CategoriaResponse response = new CategoriaResponse();
            response.setId(categoria.getId());
            response.setNome(categoria.getNome());
            response.setDescricao(categoria.getDescricao());
            response.setCreatedAt(categoria.getCreatedAt());
            response.setUpdatedAt(categoria.getUpdatedAt());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Atualizar categoria (apenas ADMIN)
    @PutMapping("/categorias/{categoriaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarCategoria(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long categoriaId,
            @RequestBody CategoriaRequest request
    ) {
        try {
            CategoriaProduto categoria = produtoService.atualizarCategoria(
                    categoriaId,
                    admin.getCompany().getId(),
                    request.getNome(),
                    request.getDescricao()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Categoria atualizada com sucesso");
            response.put("id", categoria.getId());
            response.put("nome", categoria.getNome());
            response.put("descricao", categoria.getDescricao());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Deletar categoria (apenas ADMIN)
    @DeleteMapping("/categorias/{categoriaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarCategoria(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long categoriaId
    ) {
        try {
            produtoService.deletarCategoria(categoriaId, admin.getCompany().getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Categoria deletada com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== PRODUTOS ==========

    // Criar produto (apenas ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarProduto(
            @AuthenticationPrincipal usuario admin,
            @RequestBody ProdutoRequest request
    ) {
        try {
            Produto produto = produtoService.criarProduto(
                    admin.getCompany(),
                    request.getCategoriaId(),
                    request.getNome(),
                    request.getDescricao(),
                    request.getPreco(),
                    request.getQuantidade()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Produto criado com sucesso");
            response.put("id", produto.getId());
            response.put("nome", produto.getNome());
            response.put("preco", produto.getPreco());
            response.put("quantidade", produto.getQuantidade());
            response.put("categoria", produto.getCategoria().getNome());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Listar todos os produtos (com categoria, sem empresa)
    @GetMapping
    public ResponseEntity<?> listarProdutos(@AuthenticationPrincipal usuario admin) {
        try {
            List<Produto> produtos = produtoService.listarProdutos(admin.getCompany().getId());

            // Converter para resposta customizada
            List<ProdutoResponse> produtosResponse = produtos.stream().map(produto -> {
                ProdutoResponse response = new ProdutoResponse();
                response.setId(produto.getId());
                response.setNome(produto.getNome());
                response.setDescricao(produto.getDescricao());
                response.setPreco(produto.getPreco());
                response.setQuantidade(produto.getQuantidade());
                response.setCreatedAt(produto.getCreatedAt());
                response.setUpdatedAt(produto.getUpdatedAt());

                if (produto.getCategoria() != null) {
                    ProdutoResponse.CategoriaInfo categoriaInfo = new ProdutoResponse.CategoriaInfo(
                            produto.getCategoria().getId(),
                            produto.getCategoria().getNome(),
                            produto.getCategoria().getDescricao()
                    );
                    response.setCategoria(categoriaInfo);
                }

                return response;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", produtosResponse.size());
            response.put("produtos", produtosResponse);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Listar produtos por categoria (com categoria, sem empresa)
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<?> listarProdutosPorCategoria(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long categoriaId
    ) {
        try {
            List<Produto> produtos = produtoService.listarProdutosPorCategoria(categoriaId, admin.getCompany().getId());

            // Converter para resposta customizada
            List<ProdutoResponse> produtosResponse = produtos.stream().map(produto -> {
                ProdutoResponse response = new ProdutoResponse();
                response.setId(produto.getId());
                response.setNome(produto.getNome());
                response.setDescricao(produto.getDescricao());
                response.setPreco(produto.getPreco());
                response.setQuantidade(produto.getQuantidade());
                response.setCreatedAt(produto.getCreatedAt());
                response.setUpdatedAt(produto.getUpdatedAt());

                if (produto.getCategoria() != null) {
                    ProdutoResponse.CategoriaInfo categoriaInfo = new ProdutoResponse.CategoriaInfo(
                            produto.getCategoria().getId(),
                            produto.getCategoria().getNome(),
                            produto.getCategoria().getDescricao()
                    );
                    response.setCategoria(categoriaInfo);
                }

                return response;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", produtosResponse.size());
            response.put("produtos", produtosResponse);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Buscar produto por ID (com categoria, sem empresa)
    @GetMapping("/{produtoId}")
    public ResponseEntity<?> buscarProdutoPorId(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long produtoId
    ) {
        try {
            Produto produto = produtoService.buscarProdutoPorId(produtoId, admin.getCompany().getId());

            ProdutoResponse response = new ProdutoResponse();
            response.setId(produto.getId());
            response.setNome(produto.getNome());
            response.setDescricao(produto.getDescricao());
            response.setPreco(produto.getPreco());
            response.setQuantidade(produto.getQuantidade());
            response.setCreatedAt(produto.getCreatedAt());
            response.setUpdatedAt(produto.getUpdatedAt());

            if (produto.getCategoria() != null) {
                ProdutoResponse.CategoriaInfo categoriaInfo = new ProdutoResponse.CategoriaInfo(
                        produto.getCategoria().getId(),
                        produto.getCategoria().getNome(),
                        produto.getCategoria().getDescricao()
                );
                response.setCategoria(categoriaInfo);
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Buscar produtos por nome (com categoria, sem empresa)
    @GetMapping("/search")
    public ResponseEntity<?> buscarProdutosPorNome(
            @AuthenticationPrincipal usuario admin,
            @RequestParam String nome
    ) {
        try {
            List<Produto> produtos = produtoService.buscarProdutosPorNome(admin.getCompany().getId(), nome);

            // Converter para resposta customizada
            List<ProdutoResponse> produtosResponse = produtos.stream().map(produto -> {
                ProdutoResponse response = new ProdutoResponse();
                response.setId(produto.getId());
                response.setNome(produto.getNome());
                response.setDescricao(produto.getDescricao());
                response.setPreco(produto.getPreco());
                response.setQuantidade(produto.getQuantidade());
                response.setCreatedAt(produto.getCreatedAt());
                response.setUpdatedAt(produto.getUpdatedAt());

                if (produto.getCategoria() != null) {
                    ProdutoResponse.CategoriaInfo categoriaInfo = new ProdutoResponse.CategoriaInfo(
                            produto.getCategoria().getId(),
                            produto.getCategoria().getNome(),
                            produto.getCategoria().getDescricao()
                    );
                    response.setCategoria(categoriaInfo);
                }

                return response;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", produtosResponse.size());
            response.put("produtos", produtosResponse);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Buscar produtos com estoque baixo (com categoria, sem empresa)
    @GetMapping("/estoque-baixo")
    public ResponseEntity<?> buscarProdutosEstoqueBaixo(
            @AuthenticationPrincipal usuario admin,
            @RequestParam(defaultValue = "10") Integer minimo
    ) {
        try {
            List<Produto> produtos = produtoService.buscarProdutosEstoqueBaixo(admin.getCompany().getId(), minimo);

            // Converter para resposta customizada
            List<ProdutoResponse> produtosResponse = produtos.stream().map(produto -> {
                ProdutoResponse response = new ProdutoResponse();
                response.setId(produto.getId());
                response.setNome(produto.getNome());
                response.setDescricao(produto.getDescricao());
                response.setPreco(produto.getPreco());
                response.setQuantidade(produto.getQuantidade());
                response.setCreatedAt(produto.getCreatedAt());
                response.setUpdatedAt(produto.getUpdatedAt());

                if (produto.getCategoria() != null) {
                    ProdutoResponse.CategoriaInfo categoriaInfo = new ProdutoResponse.CategoriaInfo(
                            produto.getCategoria().getId(),
                            produto.getCategoria().getNome(),
                            produto.getCategoria().getDescricao()
                    );
                    response.setCategoria(categoriaInfo);
                }

                return response;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", produtosResponse.size());
            response.put("produtos", produtosResponse);
            response.put("mensagem", produtosResponse.isEmpty() ? "Nenhum produto com estoque baixo" : "Produtos com estoque abaixo de " + minimo);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Atualizar produto (apenas ADMIN)
    @PutMapping("/{produtoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarProduto(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long produtoId,
            @RequestBody ProdutoRequest request
    ) {
        try {
            Produto produto = produtoService.atualizarProduto(
                    produtoId,
                    admin.getCompany().getId(),
                    request.getNome(),
                    request.getDescricao(),
                    request.getPreco(),
                    request.getQuantidade(),
                    request.getCategoriaId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Produto atualizado com sucesso");
            response.put("id", produto.getId());
            response.put("nome", produto.getNome());
            response.put("preco", produto.getPreco());
            response.put("quantidade", produto.getQuantidade());
            response.put("categoria", produto.getCategoria().getNome());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Deletar produto (apenas ADMIN)
    @DeleteMapping("/{produtoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarProduto(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long produtoId
    ) {
        try {
            produtoService.deletarProduto(produtoId, admin.getCompany().getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Produto deletado com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}