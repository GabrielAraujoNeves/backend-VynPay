package VynPay.Vynpay.controller;

import VynPay.Vynpay.dto.request.*;
import VynPay.Vynpay.dto.response.*;
import VynPay.Vynpay.enun.FormaPagamento;
import VynPay.Vynpay.enun.StatusComanda;
import VynPay.Vynpay.enun.TipoComanda;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comanda")
@CrossOrigin(origins = "*")
public class ComandaController {

    @Autowired
    private ComandaService comandaService;

    // ========== MESAS ==========

    @PostMapping("/mesas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarMesa(
            @AuthenticationPrincipal usuario admin,
            @RequestBody MesaRequest request
    ) {
        try {
            Mesa mesa = comandaService.criarMesa(
                    admin.getCompany(),
                    request.getNumeroMesa(),
                    request.getCapacidade()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mesa criada com sucesso");
            response.put("id", mesa.getId());
            response.put("numeroMesa", mesa.getNumeroMesa());
            response.put("capacidade", mesa.getCapacidade());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/mesas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarMesas(@AuthenticationPrincipal usuario admin) {
        try {
            List<MesaResponseDTO> mesas = comandaService.listarMesasDTO(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("total", mesas.size());
            response.put("mesas", mesas);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/mesas/detalhadas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarMesasComConsumo(@AuthenticationPrincipal usuario admin) {
        try {
            List<MesaDetalhadaResponseDTO> mesas = comandaService.listarMesasComConsumo(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("total", mesas.size());
            response.put("mesas", mesas);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/mesas/ocupadas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarMesasOcupadas(@AuthenticationPrincipal usuario admin) {
        try {
            List<Mesa> mesas = comandaService.listarMesasOcupadas(admin.getCompany().getId());

            List<MesaResponseDTO> mesasDTO = mesas.stream()
                    .map(mesa -> new MesaResponseDTO(
                            mesa.getId(),
                            mesa.getNumeroMesa(),
                            mesa.getCapacidade(),
                            mesa.getIsOcupada(),
                            mesa.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("total", mesasDTO.size());
            response.put("mesas", mesasDTO);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/mesas/{mesaId}/liberar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> liberarMesa(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long mesaId
    ) {
        try {
            comandaService.liberarMesa(mesaId, admin.getCompany().getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Mesa liberada com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/mesas/{mesaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletarMesa(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long mesaId
    ) {
        try {
            comandaService.deletarMesa(mesaId, admin.getCompany().getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "✅ Mesa deletada com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());

            // 🔥 Mensagem amigável para o usuário
            if (e.getMessage().contains("comanda aberta")) {
                error.put("error", "Não foi possível deletar a mesa pois existe uma comanda aberta. Feche a comanda primeiro.");
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== PULSEIRAS ==========

    @PostMapping("/pulseiras")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarPulseira(
            @AuthenticationPrincipal usuario admin,
            @RequestBody PulseiraRequest request
    ) {
        try {
            Pulseira pulseira = comandaService.criarPulseira(
                    admin.getCompany(),
                    request.getNumeroPulseira(),
                    request.getNomeCliente()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pulseira criada com sucesso");
            response.put("id", pulseira.getId());
            response.put("numeroPulseira", pulseira.getNumeroPulseira());
            response.put("nomeCliente", pulseira.getNomeCliente());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/pulseiras")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarPulseiras(@AuthenticationPrincipal usuario admin) {
        try {
            List<Pulseira> pulseiras = comandaService.listarPulseiras(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("total", pulseiras.size());
            response.put("pulseiras", pulseiras);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/pulseiras/ativas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarPulseirasAtivas(@AuthenticationPrincipal usuario admin) {
        try {
            List<Pulseira> pulseiras = comandaService.listarPulseirasAtivas(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("total", pulseiras.size());
            response.put("pulseiras", pulseiras);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/pulseiras/agrupar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> agruparPulseiras(
            @AuthenticationPrincipal usuario admin,
            @RequestBody AgruparPulseiraRequest request
    ) {
        try {
            comandaService.agruparPulseiras(
                    request.getPulseiraPrincipal(),
                    request.getPulseiraSecundaria(),
                    admin.getCompany().getId()
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "Pulseiras agrupadas com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/pulseiras/{numeroPulseira}/desagrupar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> desagruparPulseira(
            @AuthenticationPrincipal usuario admin,
            @PathVariable String numeroPulseira
    ) {
        try {
            comandaService.desagruparPulseira(numeroPulseira, admin.getCompany().getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Pulseira desagrupada com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== CARTÕES DE EDIFICAÇÃO ==========

    @PostMapping("/cartoes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarCartaoEdificacao(
            @AuthenticationPrincipal usuario admin,
            @RequestBody CartaoEdificacaoRequest request
    ) {
        try {
            CartaoEdificacao cartao = comandaService.criarCartaoEdificacao(
                    admin.getCompany(),
                    request.getNumeroCartao(),
                    request.getNomeCliente()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cartão criado com sucesso");
            response.put("id", cartao.getId());
            response.put("numeroCartao", cartao.getNumeroCartao());
            response.put("nomeCliente", cartao.getNomeCliente());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/cartoes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarCartoesEdificacao(@AuthenticationPrincipal usuario admin) {
        try {
            List<CartaoEdificacao> cartoes = comandaService.listarCartoesEdificacao(admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("total", cartoes.size());
            response.put("cartoes", cartoes);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/cartoes/vincular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> vincularCartoes(
            @AuthenticationPrincipal usuario admin,
            @RequestBody VincularCartaoRequest request
    ) {
        try {
            comandaService.vincularCartoes(
                    request.getCartaoPrincipal(),
                    request.getCartaoSecundario(),
                    admin.getCompany().getId()
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "Cartões vinculados com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/cartoes/{numeroCartao}/desvincular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> desvincularCartao(
            @AuthenticationPrincipal usuario admin,
            @PathVariable String numeroCartao
    ) {
        try {
            comandaService.desvincularCartao(numeroCartao, admin.getCompany().getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Cartão desvinculado com sucesso");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/cartoes/{numeroCartao}/vinculados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarCartoesVinculados(
            @AuthenticationPrincipal usuario admin,
            @PathVariable String numeroCartao
    ) {
        try {
            List<CartaoEdificacao> cartoes = comandaService.listarCartoesVinculados(numeroCartao, admin.getCompany().getId());

            Map<String, Object> response = new HashMap<>();
            response.put("total", cartoes.size());
            response.put("cartoes", cartoes);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== COMANDAS ==========

    @PostMapping("/abrir/mesa")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> abrirComandaMesa(
            @AuthenticationPrincipal usuario admin,
            @RequestParam Long mesaId,
            @RequestBody List<String> nomesClientes
    ) {
        try {
            Comanda comanda = comandaService.abrirComandaMesa(
                    admin.getCompany(),
                    mesaId,
                    nomesClientes
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comanda de mesa aberta com sucesso");
            response.put("numeroComanda", comanda.getNumeroComanda());
            response.put("tipoComanda", comanda.getTipoComanda());
            response.put("identificador", comanda.getIdentificadorComanda());
            response.put("dataAbertura", comanda.getDataAbertura());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/abrir/pulseira")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> abrirComandaPulseira(
            @AuthenticationPrincipal usuario admin,
            @RequestParam String numeroPulseira,
            @RequestParam(required = false) String nomeCliente
    ) {
        try {
            Comanda comanda = comandaService.abrirComandaPulseira(
                    admin.getCompany(),
                    numeroPulseira,
                    nomeCliente
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comanda de pulseira aberta com sucesso");
            response.put("numeroComanda", comanda.getNumeroComanda());
            response.put("tipoComanda", comanda.getTipoComanda());
            response.put("identificador", comanda.getIdentificadorComanda());
            response.put("dataAbertura", comanda.getDataAbertura());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/abrir/cartao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> abrirComandaCartao(
            @AuthenticationPrincipal usuario admin,
            @RequestParam String numeroCartao,
            @RequestParam(required = false) String nomeCliente
    ) {
        try {
            Comanda comanda = comandaService.abrirComandaCartaoEdificacao(
                    admin.getCompany(),
                    numeroCartao,
                    nomeCliente
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comanda de cartão aberta com sucesso");
            response.put("numeroComanda", comanda.getNumeroComanda());
            response.put("tipoComanda", comanda.getTipoComanda());
            response.put("identificador", comanda.getIdentificadorComanda());
            response.put("dataAbertura", comanda.getDataAbertura());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> buscarComandaPorIdentificador(
            @AuthenticationPrincipal usuario admin,
            @RequestParam String identificador,
            @RequestParam TipoComanda tipo
    ) {
        try {
            Comanda comanda = comandaService.buscarComandaPorIdentificador(
                    identificador,
                    tipo,
                    admin.getCompany().getId()
            );

            return ResponseEntity.ok(comanda);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/{comandaId}/clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarClientesDaComanda(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long comandaId
    ) {
        try {
            List<ClienteComanda> clientes = comandaService.listarClientesDaComanda(comandaId, admin.getCompany().getId());

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

    @PostMapping("/{comandaId}/cliente/{clienteComandaId}/adicionar-produto")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adicionarProdutoPorCliente(
            @AuthenticationPrincipal usuario admin,
            @PathVariable Long comandaId,
            @PathVariable Long clienteComandaId,
            @RequestBody AdicionarProdutoRequest request
    ) {
        try {
            ComandaItem item = comandaService.adicionarProdutoNaComandaPorCliente(
                    comandaId,
                    clienteComandaId,
                    request.getProdutoId(),
                    request.getQuantidade(),
                    admin.getCompany().getId()
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

    @GetMapping("/agrupadas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> buscarComandasAgrupadas(
            @AuthenticationPrincipal usuario admin,
            @RequestParam String identificador,
            @RequestParam TipoComanda tipo
    ) {
        try {
            List<Comanda> comandas = comandaService.buscarComandasAgrupadas(
                    identificador,
                    tipo,
                    admin.getCompany().getId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("total", comandas.size());
            response.put("comandas", comandas);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/pagamento/agrupado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> realizarPagamentoAgrupado(
            @AuthenticationPrincipal usuario admin,
            @RequestParam String identificador,
            @RequestParam TipoComanda tipo,
            @RequestParam BigDecimal valorPago,
            @RequestParam FormaPagamento formaPagamento
    ) {
        try {
            Pagamento pagamento = comandaService.realizarPagamentoAgrupado(
                    identificador,
                    tipo,
                    valorPago,
                    formaPagamento,
                    admin.getCompany().getId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pagamento realizado com sucesso");
            response.put("valorPago", pagamento.getValorPago());
            response.put("formaPagamento", pagamento.getFormaPagamento());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ========== MÉTODOS EXISTENTES ==========

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
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/{comandaId}/itens")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
            RelatorioResponseDTO relatorio = comandaService.gerarRelatorioDia(admin.getCompany().getId());
            return ResponseEntity.ok(relatorio);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}