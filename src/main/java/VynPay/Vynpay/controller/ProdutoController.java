package VynPay.Vynpay.controller;

import VynPay.Vynpay.dto.request.ProdutoRequest;
import VynPay.Vynpay.dto.response.HappyHourConfigResponse;
import VynPay.Vynpay.model.Produto;
import VynPay.Vynpay.model.usuario;
import VynPay.Vynpay.service.HappyHourService;
import VynPay.Vynpay.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
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

    @Autowired
    private HappyHourService happyHourService;

    // ========== PRODUTOS ==========

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
            response.put("categoria", produto.getCategoria() != null ? produto.getCategoria().getNome() : null);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping
    public ResponseEntity<?> listarProdutos(@AuthenticationPrincipal usuario admin) {
        try {
            List<Produto> produtos = produtoService.listarProdutos(admin.getCompany().getId());
            HappyHourConfigResponse activeConfig = happyHourService.getActiveConfig(admin.getCompany());

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
                produtoMap.put("quantidade", produto.getQuantidade());
                produtoMap.put("createdAt", produto.getCreatedAt());
                produtoMap.put("updatedAt", produto.getUpdatedAt());

                boolean produtoEmPromocao = false;
                BigDecimal precoFinal = produto.getPreco();
                Double descontoAplicado = 0.0;

                if (happyHourAtivo && configFinal != null) {
                    produtoEmPromocao = happyHourService.isProductInHappyHour(configFinal, produto.getId());
                    if (produtoEmPromocao) {
                        descontoAplicado = descontoFinal;
                        BigDecimal desconto = produto.getPreco()
                                .multiply(BigDecimal.valueOf(descontoAplicado / 100));
                        precoFinal = produto.getPreco().subtract(desconto)
                                .setScale(2, RoundingMode.HALF_UP);
                    }
                }

                produtoMap.put("preco", precoFinal);
                produtoMap.put("precoOriginal", produto.getPreco());
                produtoMap.put("descontoPercent", produtoEmPromocao ? descontoAplicado : 0.0);
                produtoMap.put("isInHappyHour", produtoEmPromocao);

                if (produto.getCategoria() != null) {
                    Map<String, Object> categoriaInfo = new HashMap<>();
                    categoriaInfo.put("id", produto.getCategoria().getId());
                    categoriaInfo.put("nome", produto.getCategoria().getNome());
                    categoriaInfo.put("descricao", produto.getCategoria().getDescricao());
                    categoriaInfo.put("isAtivo", produto.getCategoria().isAtivo());
                    produtoMap.put("categoria", categoriaInfo);
                }

                return produtoMap;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", produtosResponse.size());
            response.put("produtos", produtosResponse);
            response.put("isHappyHourActive", happyHourAtivo);

            if (happyHourAtivo) {
                response.put("happyHourMessage", "🔥 HAPPY HOUR ATIVO! " + descontoGlobal + "% de desconto! 🔥");
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<?> listarProdutosPorCategoria(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long categoriaId
    ) {
        try {
            List<Produto> produtos = produtoService.listarProdutosPorCategoria(categoriaId, admin.getCompany().getId());
            HappyHourConfigResponse activeConfig = happyHourService.getActiveConfig(admin.getCompany());

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
                produtoMap.put("quantidade", produto.getQuantidade());
                produtoMap.put("createdAt", produto.getCreatedAt());
                produtoMap.put("updatedAt", produto.getUpdatedAt());

                boolean produtoEmPromocao = false;
                BigDecimal precoFinal = produto.getPreco();
                Double descontoAplicado = 0.0;

                if (happyHourAtivo && configFinal != null) {
                    produtoEmPromocao = happyHourService.isProductInHappyHour(configFinal, produto.getId());
                    if (produtoEmPromocao) {
                        descontoAplicado = descontoFinal;
                        BigDecimal desconto = produto.getPreco()
                                .multiply(BigDecimal.valueOf(descontoAplicado / 100));
                        precoFinal = produto.getPreco().subtract(desconto)
                                .setScale(2, RoundingMode.HALF_UP);
                    }
                }

                produtoMap.put("preco", precoFinal);
                produtoMap.put("precoOriginal", produto.getPreco());
                produtoMap.put("descontoPercent", produtoEmPromocao ? descontoAplicado : 0.0);
                produtoMap.put("isInHappyHour", produtoEmPromocao);

                if (produto.getCategoria() != null) {
                    Map<String, Object> categoriaInfo = new HashMap<>();
                    categoriaInfo.put("id", produto.getCategoria().getId());
                    categoriaInfo.put("nome", produto.getCategoria().getNome());
                    categoriaInfo.put("descricao", produto.getCategoria().getDescricao());
                    produtoMap.put("categoria", categoriaInfo);
                }

                return produtoMap;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", produtosResponse.size());
            response.put("produtos", produtosResponse);
            response.put("isHappyHourActive", happyHourAtivo);

            if (happyHourAtivo) {
                response.put("happyHourMessage", "🔥 HAPPY HOUR ATIVO! " + descontoGlobal + "% de desconto! 🔥");
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/{produtoId}")
    public ResponseEntity<?> buscarProdutoPorId(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long produtoId
    ) {
        try {
            Produto produto = produtoService.buscarProdutoPorId(produtoId, admin.getCompany().getId());
            HappyHourConfigResponse activeConfig = happyHourService.getActiveConfig(admin.getCompany());

            boolean isHappyHourActive = false;
            Double descontoGlobal = 0.0;

            if (activeConfig != null && activeConfig.getIsActive()) {
                isHappyHourActive = isWithinTimeRange(activeConfig);
                if (isHappyHourActive) {
                    descontoGlobal = activeConfig.getDiscountPercent();
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", produto.getId());
            response.put("nome", produto.getNome());
            response.put("descricao", produto.getDescricao());
            response.put("quantidade", produto.getQuantidade());
            response.put("createdAt", produto.getCreatedAt());
            response.put("updatedAt", produto.getUpdatedAt());

            boolean produtoEmPromocao = false;
            BigDecimal precoFinal = produto.getPreco();
            Double descontoAplicado = 0.0;

            if (isHappyHourActive && activeConfig != null) {
                produtoEmPromocao = happyHourService.isProductInHappyHour(activeConfig, produto.getId());
                if (produtoEmPromocao) {
                    descontoAplicado = descontoGlobal;
                    BigDecimal desconto = produto.getPreco()
                            .multiply(BigDecimal.valueOf(descontoAplicado / 100));
                    precoFinal = produto.getPreco().subtract(desconto)
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }

            response.put("preco", precoFinal);
            response.put("precoOriginal", produto.getPreco());
            response.put("descontoPercent", produtoEmPromocao ? descontoAplicado : 0.0);
            response.put("isInHappyHour", produtoEmPromocao);

            if (produto.getCategoria() != null) {
                Map<String, Object> categoriaInfo = new HashMap<>();
                categoriaInfo.put("id", produto.getCategoria().getId());
                categoriaInfo.put("nome", produto.getCategoria().getNome());
                categoriaInfo.put("descricao", produto.getCategoria().getDescricao());
                categoriaInfo.put("isAtivo", produto.getCategoria().isAtivo());
                response.put("categoria", categoriaInfo);
            }

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> buscarProdutosPorNome(
            @AuthenticationPrincipal usuario admin,
            @RequestParam String nome
    ) {
        try {
            List<Produto> produtos = produtoService.buscarProdutosPorNome(admin.getCompany().getId(), nome);
            HappyHourConfigResponse activeConfig = happyHourService.getActiveConfig(admin.getCompany());

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
                produtoMap.put("quantidade", produto.getQuantidade());
                produtoMap.put("createdAt", produto.getCreatedAt());
                produtoMap.put("updatedAt", produto.getUpdatedAt());

                boolean produtoEmPromocao = false;
                BigDecimal precoFinal = produto.getPreco();
                Double descontoAplicado = 0.0;

                if (happyHourAtivo && configFinal != null) {
                    produtoEmPromocao = happyHourService.isProductInHappyHour(configFinal, produto.getId());
                    if (produtoEmPromocao) {
                        descontoAplicado = descontoFinal;
                        BigDecimal desconto = produto.getPreco()
                                .multiply(BigDecimal.valueOf(descontoAplicado / 100));
                        precoFinal = produto.getPreco().subtract(desconto)
                                .setScale(2, RoundingMode.HALF_UP);
                    }
                }

                produtoMap.put("preco", precoFinal);
                produtoMap.put("precoOriginal", produto.getPreco());
                produtoMap.put("descontoPercent", produtoEmPromocao ? descontoAplicado : 0.0);
                produtoMap.put("isInHappyHour", produtoEmPromocao);

                if (produto.getCategoria() != null) {
                    Map<String, Object> categoriaInfo = new HashMap<>();
                    categoriaInfo.put("id", produto.getCategoria().getId());
                    categoriaInfo.put("nome", produto.getCategoria().getNome());
                    categoriaInfo.put("descricao", produto.getCategoria().getDescricao());
                    produtoMap.put("categoria", categoriaInfo);
                }

                return produtoMap;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", produtosResponse.size());
            response.put("produtos", produtosResponse);
            response.put("isHappyHourActive", happyHourAtivo);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/estoque-baixo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> buscarProdutosEstoqueBaixo(
            @AuthenticationPrincipal usuario admin,
            @RequestParam(defaultValue = "10") Integer minimo
    ) {
        try {
            List<Produto> produtos = produtoService.buscarProdutosEstoqueBaixo(admin.getCompany().getId(), minimo);

            List<Map<String, Object>> produtosResponse = produtos.stream().map(produto -> {
                Map<String, Object> produtoMap = new HashMap<>();
                produtoMap.put("id", produto.getId());
                produtoMap.put("nome", produto.getNome());
                produtoMap.put("descricao", produto.getDescricao());
                produtoMap.put("preco", produto.getPreco());
                produtoMap.put("quantidade", produto.getQuantidade());
                produtoMap.put("createdAt", produto.getCreatedAt());
                produtoMap.put("updatedAt", produto.getUpdatedAt());
                produtoMap.put("estoqueMinimo", minimo);
                produtoMap.put("status", produto.getQuantidade() <= minimo ? "⚠️ ESTOQUE BAIXO" : "✅ Estoque OK");

                if (produto.getCategoria() != null) {
                    Map<String, Object> categoriaInfo = new HashMap<>();
                    categoriaInfo.put("id", produto.getCategoria().getId());
                    categoriaInfo.put("nome", produto.getCategoria().getNome());
                    categoriaInfo.put("descricao", produto.getCategoria().getDescricao());
                    produtoMap.put("categoria", categoriaInfo);
                }
                return produtoMap;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", produtosResponse.size());
            response.put("produtos", produtosResponse);
            response.put("mensagem", produtosResponse.isEmpty() ? "✅ Nenhum produto com estoque baixo" : "⚠️ Produtos com estoque abaixo de " + minimo);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

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
            response.put("categoria", produto.getCategoria() != null ? produto.getCategoria().getNome() : null);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PatchMapping("/{produtoId}/estoque")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> atualizarEstoque(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long produtoId,
            @RequestParam Integer quantidade
    ) {
        try {
            Produto produto = produtoService.buscarProdutoPorId(produtoId, admin.getCompany().getId());
            produto.setQuantidade(quantidade);
            produto = produtoService.atualizarProduto(
                    produtoId,
                    admin.getCompany().getId(),
                    produto.getNome(),
                    produto.getDescricao(),
                    produto.getPreco(),
                    quantidade,
                    produto.getCategoria() != null ? produto.getCategoria().getId() : null
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Estoque atualizado com sucesso");
            response.put("id", produto.getId());
            response.put("nome", produto.getNome());
            response.put("quantidade", produto.getQuantidade());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

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

    // ========== MÉTODO AUXILIAR ==========

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