package VynPay.Vynpay.service;

import VynPay.Vynpay.dto.request.*;
import VynPay.Vynpay.dto.response.*;
import VynPay.Vynpay.enun.FormaPagamento;
import VynPay.Vynpay.enun.StatusComanda;
import VynPay.Vynpay.enun.TipoComanda;
import VynPay.Vynpay.model.*;
import VynPay.Vynpay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ComandaService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ComandaRepository comandaRepository;

    @Autowired
    private ComandaItemRepository comandaItemRepository;

    @Autowired
    private ClienteComandaRepository clienteComandaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private MesaRepository mesaRepository;

    @Autowired
    private PulseiraRepository pulseiraRepository;

    @Autowired
    private CartaoEdificacaoRepository cartaoEdificacaoRepository;

    // ========== CLIENTES ==========

    @Transactional
    public Cliente cadastrarCliente(Company company, String nome, String cpf, String telefone) {
        if (clienteRepository.findByCpf(cpf).isPresent()) {
            throw new RuntimeException("Cliente com este CPF já existe");
        }

        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpf(cpf);
        cliente.setTelefone(telefone);
        cliente.setCompany(company);
        cliente.setComandaAtiva(false);

        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarClientes(Long companyId) {
        return clienteRepository.findByCompanyId(companyId);
    }

    // ========== ITENS DA COMANDA ==========

    public List<ComandaItem> listarItensDaComanda(Long comandaId, Long companyId) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        if (!comanda.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Comanda não pertence à sua empresa");
        }

        return comandaItemRepository.findByComandaId(comandaId);
    }

    @Transactional
    public void removerItemDaComanda(Long itemId, Long companyId) {
        ComandaItem item = comandaItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        Comanda comanda = item.getComanda();

        if (!comanda.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Item não pertence à sua empresa");
        }

        if (comanda.getStatus() != StatusComanda.ABERTA) {
            throw new RuntimeException("Comanda já foi fechada ou paga");
        }

        Produto produto = item.getProduto();
        produto.setQuantidade(produto.getQuantidade() + item.getQuantidade());
        produtoRepository.save(produto);

        comanda.setValorTotal(comanda.getValorTotal().subtract(item.getPrecoTotal()));
        comandaRepository.save(comanda);

        comandaItemRepository.delete(item);
    }

    // ========== FECHAMENTO E PAGAMENTO ==========

    @Transactional
    public Comanda fecharComanda(Long comandaId, Long companyId) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        if (!comanda.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Comanda não pertence à sua empresa");
        }

        if (comanda.getStatus() != StatusComanda.ABERTA) {
            throw new RuntimeException("Comanda já foi fechada ou paga");
        }

        comanda.setStatus(StatusComanda.FECHADA);
        comanda.setDataFechamento(LocalDateTime.now());

        return comandaRepository.save(comanda);
    }

    @Transactional
    public Pagamento realizarPagamento(Long comandaId, BigDecimal valorPago, FormaPagamento formaPagamento, Long companyId) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        if (!comanda.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Comanda não pertence à sua empresa");
        }

        if (comanda.getStatus() == StatusComanda.PAGA) {
            throw new RuntimeException("Comanda já foi paga");
        }

        if (valorPago.compareTo(comanda.getValorTotal()) < 0) {
            throw new RuntimeException("Valor pago é menor que o valor total da comanda");
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setComanda(comanda);
        pagamento.setValorPago(valorPago);
        pagamento.setFormaPagamento(formaPagamento);

        comanda.setStatus(StatusComanda.PAGA);

        Cliente cliente = comanda.getCliente();
        if (cliente != null) {
            cliente.setComandaAtiva(false);
            clienteRepository.save(cliente);
        }

        comandaRepository.save(comanda);

        return pagamentoRepository.save(pagamento);
    }

    public BigDecimal calcularTotalDoDia(Long companyId) {
        List<Pagamento> pagamentos = pagamentoRepository.findAll();
        return pagamentos.stream()
                .filter(p -> p.getDataPagamento().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .filter(p -> p.getComanda().getCompany().getId().equals(companyId))
                .map(Pagamento::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Comanda> listarComandasPorStatus(Long companyId, StatusComanda status) {
        return comandaRepository.findByCompanyIdAndStatus(companyId, status);
    }

    // ========== MESAS ==========

    @Transactional
    public Mesa criarMesa(Company company, Integer numeroMesa, Integer capacidade) {
        if (mesaRepository.findByNumeroMesaAndCompanyId(numeroMesa, company.getId()).isPresent()) {
            throw new RuntimeException("Mesa já existe");
        }

        Mesa mesa = new Mesa();
        mesa.setNumeroMesa(numeroMesa);
        mesa.setCapacidade(capacidade);
        mesa.setIsOcupada(false);
        mesa.setCompany(company);

        return mesaRepository.save(mesa);
    }

    public List<Mesa> listarMesas(Long companyId) {
        return mesaRepository.findByCompanyId(companyId);
    }

    public List<Mesa> listarMesasOcupadas(Long companyId) {
        return mesaRepository.findByCompanyIdAndIsOcupadaTrue(companyId);
    }

    public List<MesaResponseDTO> listarMesasDTO(Long companyId) {
        List<Mesa> mesas = mesaRepository.findByCompanyId(companyId);

        return mesas.stream()
                .map(mesa -> new MesaResponseDTO(
                        mesa.getId(),
                        mesa.getNumeroMesa(),
                        mesa.getCapacidade(),
                        mesa.getIsOcupada(),
                        mesa.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<MesaDetalhadaResponseDTO> listarMesasComConsumo(Long companyId) {
        List<Mesa> mesas = mesaRepository.findByCompanyId(companyId);

        return mesas.stream().map(mesa -> {
            // Buscar comanda ativa da mesa
            List<Comanda> comandas = comandaRepository.findByIdentificadorAndTipoAndCompanyId(
                    String.valueOf(mesa.getNumeroMesa()),
                    TipoComanda.MESA,
                    companyId
            );

            // Filtrar apenas comandas abertas ou ativas
            Comanda comandaAtiva = comandas.stream()
                    .filter(c -> c.getStatus() == StatusComanda.ABERTA)
                    .findFirst()
                    .orElse(null);

            ComandaInfoDTO comandaInfo = null;

            if (comandaAtiva != null) {
                // Buscar clientes da comanda
                List<ClienteComanda> clientes = clienteComandaRepository.findByComandaId(comandaAtiva.getId());

                List<ClienteConsumoDTO> clientesConsumo = clientes.stream().map(cliente -> {
                    // Buscar itens do cliente
                    List<ComandaItem> itensCliente = comandaItemRepository.findByComandaId(comandaAtiva.getId());

                    List<ItemConsumoDTO> itensConsumo = itensCliente.stream()
                            .map(item -> new ItemConsumoDTO(
                                    item.getId(),
                                    item.getProduto().getNome(),
                                    item.getQuantidade(),
                                    item.getPrecoUnitario(),
                                    item.getPrecoTotal()
                            )).collect(Collectors.toList());

                    return new ClienteConsumoDTO(
                            cliente.getId(),
                            cliente.getNome(),
                            cliente.getValorTotal(),
                            itensConsumo
                    );
                }).collect(Collectors.toList());

                comandaInfo = new ComandaInfoDTO(
                        comandaAtiva.getId(),
                        comandaAtiva.getNumeroComanda(),
                        comandaAtiva.getDataAbertura(),
                        comandaAtiva.getValorTotal(),
                        clientesConsumo
                );
            }

            return new MesaDetalhadaResponseDTO(
                    mesa.getId(),
                    mesa.getNumeroMesa(),
                    mesa.getCapacidade(),
                    mesa.getIsOcupada(),
                    mesa.getCreatedAt(),
                    comandaInfo
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public void liberarMesa(Long mesaId, Long companyId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        if (!mesa.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Mesa não pertence à sua empresa");
        }

        mesa.setIsOcupada(false);
        mesaRepository.save(mesa);
    }

    @Transactional
    public void deletarMesa(Long mesaId, Long companyId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        if (!mesa.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Mesa não pertence à sua empresa");
        }

        // Verificar se a mesa está ocupada
        if (mesa.getIsOcupada()) {
            throw new RuntimeException("Não é possível deletar uma mesa ocupada. Libere a mesa primeiro.");
        }

        // Verificar se existe comanda associada a esta mesa
        List<Comanda> comandas = comandaRepository.findByIdentificadorAndTipoAndCompanyId(
                String.valueOf(mesa.getNumeroMesa()),
                TipoComanda.MESA,
                companyId
        );

        if (!comandas.isEmpty()) {
            // Verificar se alguma comanda está aberta
            boolean temComandaAberta = comandas.stream()
                    .anyMatch(c -> c.getStatus() == StatusComanda.ABERTA);

            if (temComandaAberta) {
                throw new RuntimeException("Não é possível deletar a mesa porque existe uma comanda aberta. Feche a comanda primeiro.");
            }
        }

        mesaRepository.delete(mesa);
    }

    // ========== PULSEIRAS ==========

    @Transactional
    public Pulseira criarPulseira(Company company, String numeroPulseira, String nomeCliente) {
        if (pulseiraRepository.findByNumeroPulseiraAndCompanyId(numeroPulseira, company.getId()).isPresent()) {
            throw new RuntimeException("Pulseira já existe");
        }

        Pulseira pulseira = new Pulseira();
        pulseira.setNumeroPulseira(numeroPulseira);
        pulseira.setNomeCliente(nomeCliente);
        pulseira.setIsAtivo(true);
        pulseira.setCompany(company);

        return pulseiraRepository.save(pulseira);
    }

    public List<Pulseira> listarPulseiras(Long companyId) {
        return pulseiraRepository.findByCompanyId(companyId);
    }

    public List<Pulseira> listarPulseirasAtivas(Long companyId) {
        return pulseiraRepository.findByCompanyIdAndIsAtivoTrue(companyId);
    }

    @Transactional
    public void agruparPulseiras(String pulseiraPrincipal, String pulseiraSecundaria, Long companyId) {
        Pulseira principal = pulseiraRepository.findByNumeroPulseiraAndCompanyId(pulseiraPrincipal, companyId)
                .orElseThrow(() -> new RuntimeException("Pulseira principal não encontrada"));

        Pulseira secundaria = pulseiraRepository.findByNumeroPulseiraAndCompanyId(pulseiraSecundaria, companyId)
                .orElseThrow(() -> new RuntimeException("Pulseira secundária não encontrada"));

        secundaria.setPulseiraAgrupadaCom(pulseiraPrincipal);
        pulseiraRepository.save(secundaria);
    }

    @Transactional
    public void desagruparPulseira(String numeroPulseira, Long companyId) {
        Pulseira pulseira = pulseiraRepository.findByNumeroPulseiraAndCompanyId(numeroPulseira, companyId)
                .orElseThrow(() -> new RuntimeException("Pulseira não encontrada"));

        pulseira.setPulseiraAgrupadaCom(null);
        pulseiraRepository.save(pulseira);
    }

    // ========== CARTÕES DE EDIFICAÇÃO ==========

    @Transactional
    public CartaoEdificacao criarCartaoEdificacao(Company company, String numeroCartao, String nomeCliente) {
        if (cartaoEdificacaoRepository.findByNumeroCartaoAndCompanyId(numeroCartao, company.getId()).isPresent()) {
            throw new RuntimeException("Cartão já existe");
        }

        CartaoEdificacao cartao = new CartaoEdificacao();
        cartao.setNumeroCartao(numeroCartao);
        cartao.setNomeCliente(nomeCliente);
        cartao.setIsAtivo(true);
        cartao.setCompany(company);

        return cartaoEdificacaoRepository.save(cartao);
    }

    public List<CartaoEdificacao> listarCartoesEdificacao(Long companyId) {
        return cartaoEdificacaoRepository.findByCompanyId(companyId);
    }

    @Transactional
    public void vincularCartoes(String cartaoPrincipal, String cartaoSecundario, Long companyId) {
        CartaoEdificacao principal = cartaoEdificacaoRepository.findByNumeroCartaoAndCompanyId(cartaoPrincipal, companyId)
                .orElseThrow(() -> new RuntimeException("Cartão principal não encontrado"));

        CartaoEdificacao secundario = cartaoEdificacaoRepository.findByNumeroCartaoAndCompanyId(cartaoSecundario, companyId)
                .orElseThrow(() -> new RuntimeException("Cartão secundário não encontrado"));

        secundario.setCartaoVinculado(cartaoPrincipal);
        cartaoEdificacaoRepository.save(secundario);
    }

    @Transactional
    public void desvincularCartao(String numeroCartao, Long companyId) {
        CartaoEdificacao cartao = cartaoEdificacaoRepository.findByNumeroCartaoAndCompanyId(numeroCartao, companyId)
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));

        cartao.setCartaoVinculado(null);
        cartaoEdificacaoRepository.save(cartao);
    }

    public List<CartaoEdificacao> listarCartoesVinculados(String cartaoPrincipal, Long companyId) {
        // Verificar se o cartão principal existe
        cartaoEdificacaoRepository.findByNumeroCartaoAndCompanyId(cartaoPrincipal, companyId)
                .orElseThrow(() -> new RuntimeException("Cartão principal não encontrado"));

        return cartaoEdificacaoRepository.findByCartaoVinculado(cartaoPrincipal);
    }

    // ========== COMANDAS ==========

    @Transactional
    public Comanda abrirComandaMesa(Company company, Long mesaId, List<String> nomesClientes) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        if (!mesa.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Mesa não pertence à sua empresa");
        }

        if (mesa.getIsOcupada()) {
            throw new RuntimeException("Mesa já está ocupada");
        }

        if (nomesClientes.size() > mesa.getCapacidade()) {
            throw new RuntimeException("Número de clientes excede a capacidade da mesa");
        }

        String numeroComanda = "MESA-" + mesa.getNumeroMesa() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Comanda comanda = new Comanda();
        comanda.setNumeroComanda(numeroComanda);
        comanda.setTipoComanda(TipoComanda.MESA);
        comanda.setIdentificadorComanda(String.valueOf(mesa.getNumeroMesa()));
        comanda.setMesaId(mesaId);
        comanda.setCompany(company);
        comanda.setStatus(StatusComanda.ABERTA);
        comanda.setValorTotal(BigDecimal.ZERO);

        comanda = comandaRepository.save(comanda);

        for (String nome : nomesClientes) {
            ClienteComanda clienteComanda = new ClienteComanda();
            clienteComanda.setNome(nome);
            clienteComanda.setComanda(comanda);
            clienteComanda.setValorTotal(BigDecimal.ZERO);
            clienteComandaRepository.save(clienteComanda);
        }

        mesa.setIsOcupada(true);
        mesaRepository.save(mesa);

        return comanda;
    }

    @Transactional
    public Comanda abrirComandaPulseira(Company company, String numeroPulseira, String nomeCliente) {
        Pulseira pulseira = pulseiraRepository.findByNumeroPulseiraAndCompanyId(numeroPulseira, company.getId())
                .orElseThrow(() -> new RuntimeException("Pulseira não encontrada"));

        String numeroComanda = "PULSEIRA-" + numeroPulseira + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Comanda comanda = new Comanda();
        comanda.setNumeroComanda(numeroComanda);
        comanda.setTipoComanda(TipoComanda.PULSEIRA);
        comanda.setIdentificadorComanda(numeroPulseira);
        comanda.setCompany(company);
        comanda.setStatus(StatusComanda.ABERTA);
        comanda.setValorTotal(BigDecimal.ZERO);

        comanda = comandaRepository.save(comanda);

        ClienteComanda clienteComanda = new ClienteComanda();
        clienteComanda.setNome(nomeCliente != null ? nomeCliente : "Cliente Pulseira " + numeroPulseira);
        clienteComanda.setComanda(comanda);
        clienteComanda.setValorTotal(BigDecimal.ZERO);
        clienteComandaRepository.save(clienteComanda);

        return comanda;
    }

    @Transactional
    public Comanda abrirComandaCartaoEdificacao(Company company, String numeroCartao, String nomeCliente) {
        CartaoEdificacao cartao = cartaoEdificacaoRepository.findByNumeroCartaoAndCompanyId(numeroCartao, company.getId())
                .orElseThrow(() -> new RuntimeException("Cartão não encontrado"));

        String numeroComanda = "CARTAO-" + numeroCartao + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        Comanda comanda = new Comanda();
        comanda.setNumeroComanda(numeroComanda);
        comanda.setTipoComanda(TipoComanda.CARTAO_EDIFICACAO);
        comanda.setIdentificadorComanda(numeroCartao);
        comanda.setCompany(company);
        comanda.setStatus(StatusComanda.ABERTA);
        comanda.setValorTotal(BigDecimal.ZERO);

        comanda = comandaRepository.save(comanda);

        ClienteComanda clienteComanda = new ClienteComanda();
        clienteComanda.setNome(nomeCliente != null ? nomeCliente : "Cliente Cartão " + numeroCartao);
        clienteComanda.setComanda(comanda);
        clienteComanda.setValorTotal(BigDecimal.ZERO);
        clienteComandaRepository.save(clienteComanda);

        return comanda;
    }

    public Comanda buscarComandaPorIdentificador(String identificador, TipoComanda tipo, Long companyId) {
        List<Comanda> comandas = comandaRepository.findByIdentificadorAndTipoAndCompanyId(identificador, tipo, companyId);

        if (comandas.isEmpty()) {
            throw new RuntimeException("Comanda não encontrada");
        }

        // Retorna a comanda mais recente (maior ID)
        return comandas.stream()
                .max((c1, c2) -> c1.getId().compareTo(c2.getId()))
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));
    }

    public List<ClienteComanda> listarClientesDaComanda(Long comandaId, Long companyId) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        if (!comanda.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Comanda não pertence à sua empresa");
        }

        return clienteComandaRepository.findByComandaId(comandaId);
    }

    @Transactional
    public ComandaItem adicionarProdutoNaComandaPorCliente(Long comandaId, Long clienteComandaId, Long produtoId, Integer quantidade, Long companyId) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        ClienteComanda clienteComanda = clienteComandaRepository.findById(clienteComandaId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado na comanda"));

        Produto produto = produtoRepository.findByIdAndCompanyId(produtoId, companyId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (produto.getQuantidade() < quantidade) {
            throw new RuntimeException("Estoque insuficiente");
        }

        produto.setQuantidade(produto.getQuantidade() - quantidade);
        produtoRepository.save(produto);

        ComandaItem item = new ComandaItem();
        item.setComanda(comanda);
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(produto.getPreco());
        item.calcularTotal();

        clienteComanda.setValorTotal(clienteComanda.getValorTotal().add(item.getPrecoTotal()));
        clienteComandaRepository.save(clienteComanda);

        comanda.setValorTotal(comanda.getValorTotal().add(item.getPrecoTotal()));
        comandaRepository.save(comanda);

        return comandaItemRepository.save(item);
    }

    public List<Comanda> buscarComandasAgrupadas(String identificador, TipoComanda tipo, Long companyId) {
        if (tipo == TipoComanda.PULSEIRA) {
            List<Pulseira> pulseirasAgrupadas = pulseiraRepository.findByPulseiraAgrupadaCom(identificador);
            List<Comanda> comandas = new ArrayList<>();

            List<Comanda> comandaPrincipal = comandaRepository.findByIdentificadorAndTipoAndCompanyId(identificador, tipo, companyId);
            comandas.addAll(comandaPrincipal);

            for (Pulseira pulseira : pulseirasAgrupadas) {
                List<Comanda> comandaSecundaria = comandaRepository.findByIdentificadorAndTipoAndCompanyId(
                        pulseira.getNumeroPulseira(), tipo, companyId);
                comandas.addAll(comandaSecundaria);
            }
            return comandas;

        } else if (tipo == TipoComanda.CARTAO_EDIFICACAO) {
            List<CartaoEdificacao> cartoesVinculados = cartaoEdificacaoRepository.findByCartaoVinculado(identificador);
            List<Comanda> comandas = new ArrayList<>();

            List<Comanda> comandaPrincipal = comandaRepository.findByIdentificadorAndTipoAndCompanyId(identificador, tipo, companyId);
            comandas.addAll(comandaPrincipal);

            for (CartaoEdificacao cartao : cartoesVinculados) {
                List<Comanda> comandaSecundaria = comandaRepository.findByIdentificadorAndTipoAndCompanyId(
                        cartao.getNumeroCartao(), tipo, companyId);
                comandas.addAll(comandaSecundaria);
            }
            return comandas;
        }
        return List.of();
    }

    @Transactional
    public Pagamento realizarPagamentoAgrupado(String identificador, TipoComanda tipo, BigDecimal valorPago, FormaPagamento formaPagamento, Long companyId) {
        List<Comanda> comandas = buscarComandasAgrupadas(identificador, tipo, companyId);

        BigDecimal valorTotal = comandas.stream()
                .map(Comanda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (valorPago.compareTo(valorTotal) < 0) {
            throw new RuntimeException("Valor pago é menor que o valor total das comandas");
        }

        Pagamento pagamentoPrincipal = null;
        for (Comanda comanda : comandas) {
            Pagamento pagamento = new Pagamento();
            pagamento.setComanda(comanda);
            pagamento.setValorPago(comanda.getValorTotal());
            pagamento.setFormaPagamento(formaPagamento);

            comanda.setStatus(StatusComanda.PAGA);
            comandaRepository.save(comanda);

            if (pagamentoPrincipal == null) {
                pagamentoPrincipal = pagamentoRepository.save(pagamento);
            } else {
                pagamentoRepository.save(pagamento);
            }
        }

        return pagamentoPrincipal;
    }

    // ========== RELATÓRIO ==========

    public RelatorioResponseDTO gerarRelatorioDia(Long companyId) {
        LocalDate hoje = LocalDate.now();

        List<Comanda> comandasPagas = comandaRepository.findByCompanyIdAndStatus(companyId, StatusComanda.PAGA);

        BigDecimal totalVendas = comandasPagas.stream()
                .filter(c -> c.getDataFechamento() != null &&
                        c.getDataFechamento().toLocalDate().equals(hoje))
                .map(Comanda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ResumoComandaDTO> comandasResumo = comandasPagas.stream()
                .filter(c -> c.getDataFechamento() != null &&
                        c.getDataFechamento().toLocalDate().equals(hoje))
                .map(this::toResumoComandaDTO)
                .collect(Collectors.toList());

        return new RelatorioResponseDTO(
                hoje,
                totalVendas,
                (long) comandasResumo.size(),
                comandasResumo
        );
    }

    private ResumoComandaDTO toResumoComandaDTO(Comanda comanda) {
        List<ResumoItemDTO> itensDTO = comanda.getItens().stream()
                .map(item -> new ResumoItemDTO(
                        item.getId(),
                        item.getQuantidade(),
                        item.getPrecoUnitario(),
                        item.getPrecoTotal(),
                        item.getProduto().getNome(),
                        item.getProduto().getPreco()
                )).collect(Collectors.toList());

        List<ResumoClienteDTO> clientesDTO = comanda.getClientesComanda().stream()
                .map(cliente -> new ResumoClienteDTO(
                        cliente.getId(),
                        cliente.getNome(),
                        cliente.getValorTotal()
                )).collect(Collectors.toList());

        return new ResumoComandaDTO(
                comanda.getId(),
                comanda.getNumeroComanda(),
                comanda.getDataAbertura(),
                comanda.getStatus().toString(),
                comanda.getValorTotal(),
                comanda.getTipoComanda() != null ? comanda.getTipoComanda().toString() : "NORMAL",
                comanda.getIdentificadorComanda(),
                itensDTO,
                clientesDTO
        );
    }
}