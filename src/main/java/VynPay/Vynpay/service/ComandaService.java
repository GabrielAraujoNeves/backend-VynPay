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

    @Autowired
    private CompanyRepository companyRepository;

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

    public List<ComandaItem> buscarItensPorComanda(Long comandaId) {
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
        comanda.setDataFechamento(LocalDateTime.now());

        Cliente cliente = comanda.getCliente();
        if (cliente != null) {
            cliente.setComandaAtiva(false);
            clienteRepository.save(cliente);
        }

        if (comanda.getTipoComanda() == TipoComanda.MESA) {
            liberarMesaPorComanda(comanda, companyId);
        }

        comandaRepository.save(comanda);
        return pagamentoRepository.save(pagamento);
    }

    @Transactional
    public void liberarMesaPorComanda(Comanda comanda, Long companyId) {
        try {
            String identificador = comanda.getIdentificadorComanda();
            Integer numeroMesa = Integer.parseInt(identificador);

            Mesa mesa = mesaRepository.findByNumeroMesaAndCompanyId(numeroMesa, companyId)
                    .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

            // 1. Remover pagamentos
            List<Pagamento> pagamentos = pagamentoRepository.findByComandaId(comanda.getId());
            if (!pagamentos.isEmpty()) {
                pagamentoRepository.deleteAll(pagamentos);
                System.out.println("✅ " + pagamentos.size() + " pagamentos removidos");
            }

            // 2. Remover itens
            List<ComandaItem> itens = comandaItemRepository.findByComandaId(comanda.getId());
            if (!itens.isEmpty()) {
                comandaItemRepository.deleteAll(itens);
                System.out.println(itens.size() + " itens removidos da comanda");
            }

            // 3. Remover clientes
            List<ClienteComanda> clientes = clienteComandaRepository.findByComandaId(comanda.getId());
            if (!clientes.isEmpty()) {
                clienteComandaRepository.deleteAll(clientes);
                System.out.println(clientes.size() + " clientes removidos da mesa " + mesa.getNumeroMesa());
            }

            // 4. Liberar a mesa
            mesa.setIsOcupada(false);
            mesaRepository.save(mesa);

            System.out.println(" Mesa " + mesa.getNumeroMesa() + " liberada com sucesso!");

        } catch (Exception e) {
            System.err.println("❌ Erro ao liberar mesa: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== PAGAMENTO INDIVIDUAL ==========

    @Transactional
    public Pagamento pagarClienteIndividual(Long comandaId, Long clienteComandaId, BigDecimal valorPago, FormaPagamento formaPagamento, Long companyId) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        if (!comanda.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Comanda não pertence à sua empresa");
        }

        ClienteComanda cliente = clienteComandaRepository.findById(clienteComandaId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getComanda().getId().equals(comandaId)) {
            throw new RuntimeException("Cliente não pertence a esta comanda");
        }

        if (valorPago.compareTo(cliente.getValorTotal()) < 0) {
            throw new RuntimeException("Valor pago é menor que o valor total do cliente");
        }

        cliente.setPago(Boolean.TRUE);
        cliente.setDataPagamento(LocalDateTime.now());
        clienteComandaRepository.save(cliente);

        Pagamento pagamento = new Pagamento();
        pagamento.setComanda(comanda);
        pagamento.setClienteComanda(cliente);
        pagamento.setValorPago(valorPago);
        pagamento.setFormaPagamento(formaPagamento);
        pagamento.setDataPagamento(LocalDateTime.now());
        pagamentoRepository.save(pagamento);

        List<ClienteComanda> clientes = clienteComandaRepository.findByComandaId(comandaId);
        boolean todosPagos = true;

        System.out.println("🔍 Verificando clientes da comanda " + comandaId + ":");
        for (ClienteComanda c : clientes) {
            boolean pago = Boolean.TRUE.equals(c.getPago());
            System.out.println("  - " + c.getNome() + ": pago = " + pago);
            if (!pago) {
                todosPagos = false;
            }
        }

        System.out.println("✅ Todos pagaram? " + todosPagos);

        if (todosPagos) {
            System.out.println(" Todos pagaram! Fechando comanda e liberando mesa...");
            comanda.setStatus(StatusComanda.PAGA);
            comanda.setDataFechamento(LocalDateTime.now());
            comandaRepository.save(comanda);

            if (comanda.getTipoComanda() == TipoComanda.MESA) {
                liberarMesaPorComanda(comanda, companyId);
            }
        } else {
            System.out.println("⏳ Ainda há clientes pendentes...");
        }

        return pagamento;
    }

    private boolean verificarTodosClientesPagos(Long comandaId) {
        List<ClienteComanda> clientes = clienteComandaRepository.findByComandaId(comandaId);
        return clientes.stream().allMatch(c -> Boolean.TRUE.equals(c.getPago()));
    }

    // ========== PAGAMENTO CONJUNTO ==========

    @Transactional
    public List<Pagamento> pagamentoConjunto(Long comandaId, BigDecimal valorPago, FormaPagamento formaPagamento, List<Long> clienteIds, Long companyId) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        if (!comanda.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Comanda não pertence à sua empresa");
        }

        BigDecimal valorTotalClientes = BigDecimal.ZERO;
        List<ClienteComanda> clientesSelecionados = new ArrayList<>();

        for (Long clienteId : clienteIds) {
            ClienteComanda cliente = clienteComandaRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado: " + clienteId));

            if (!cliente.getComanda().getId().equals(comandaId)) {
                throw new RuntimeException("Cliente não pertence a esta comanda");
            }

            clientesSelecionados.add(cliente);
            valorTotalClientes = valorTotalClientes.add(cliente.getValorTotal());
        }

        if (valorPago.compareTo(valorTotalClientes) < 0) {
            throw new RuntimeException("Valor pago é menor que o valor total");
        }

        List<Pagamento> pagamentos = new ArrayList<>();

        for (ClienteComanda cliente : clientesSelecionados) {
            cliente.setPago(Boolean.TRUE);
            cliente.setDataPagamento(LocalDateTime.now());
            clienteComandaRepository.save(cliente);

            Pagamento pagamento = new Pagamento();
            pagamento.setComanda(comanda);
            pagamento.setClienteComanda(cliente);
            pagamento.setValorPago(cliente.getValorTotal());
            pagamento.setFormaPagamento(formaPagamento);
            pagamento.setDataPagamento(LocalDateTime.now());
            pagamentos.add(pagamentoRepository.save(pagamento));
        }

        boolean todosPagos = verificarTodosClientesPagos(comandaId);

        if (todosPagos) {
            comanda.setStatus(StatusComanda.PAGA);
            comanda.setDataFechamento(LocalDateTime.now());
            comandaRepository.save(comanda);

            if (comanda.getTipoComanda() == TipoComanda.MESA) {
                liberarMesaPorComanda(comanda, companyId);
            }
        }

        return pagamentos;
    }

    // ========== REMOVER CLIENTE DA MESA ==========

    @Transactional
    public void removerClienteDaMesa(Long comandaId, Long clienteComandaId, Long companyId) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        if (!comanda.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Comanda não pertence à sua empresa");
        }

        ClienteComanda cliente = clienteComandaRepository.findById(clienteComandaId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        if (!cliente.getComanda().getId().equals(comandaId)) {
            throw new RuntimeException("Cliente não pertence a esta comanda");
        }

        List<ComandaItem> itensCliente = comandaItemRepository.findByComandaId(comandaId);
        boolean temItens = itensCliente.stream()
                .anyMatch(item -> item.getClienteComanda() != null &&
                        item.getClienteComanda().getId().equals(clienteComandaId));

        if (temItens) {
            throw new RuntimeException("Não é possível remover o cliente pois ele já consumiu itens. Ele precisa pagar primeiro.");
        }

        clienteComandaRepository.delete(cliente);

        List<ClienteComanda> clientesRestantes = clienteComandaRepository.findByComandaId(comandaId);

        if (clientesRestantes.isEmpty()) {
            comanda.setStatus(StatusComanda.FECHADA);
            comanda.setDataFechamento(LocalDateTime.now());
            comandaRepository.save(comanda);

            if (comanda.getTipoComanda() == TipoComanda.MESA) {
                liberarMesaPorComanda(comanda, companyId);
            }
        }
    }

    // ========== ADICIONAR CLIENTE À COMANDA EXISTENTE ==========

    @Transactional
    public ClienteComanda adicionarClienteNaComanda(Long comandaId, String nomeCliente, Long companyId) {
        // 1. Buscar a comanda
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        // 2. Verificar se a comanda pertence à empresa
        if (!comanda.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Comanda não pertence à sua empresa");
        }

        // 3. Verificar se a comanda está aberta
        if (comanda.getStatus() != StatusComanda.ABERTA) {
            throw new RuntimeException("Comanda já foi fechada ou paga. Não é possível adicionar clientes.");
        }

        // 4. Verificar se é uma comanda de mesa
        if (comanda.getTipoComanda() != TipoComanda.MESA) {
            throw new RuntimeException("Esta comanda não é de uma mesa. Não é possível adicionar clientes.");
        }

        // 5. Buscar a mesa para verificar capacidade
        Mesa mesa = mesaRepository.findById(comanda.getMesaId())
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        // 6. Contar clientes atuais na comanda
        List<ClienteComanda> clientesAtuais = clienteComandaRepository.findByComandaId(comandaId);
        int totalClientes = clientesAtuais.size();

        // 7. Verificar se cabe mais um cliente
        if (totalClientes >= mesa.getCapacidade()) {
            throw new RuntimeException("Mesa está lotada! Capacidade máxima: " + mesa.getCapacidade() + " clientes.");
        }

        // 8. Verificar se o cliente já existe na comanda (mesmo nome)
        boolean clienteExiste = clientesAtuais.stream()
                .anyMatch(c -> c.getNome().equalsIgnoreCase(nomeCliente));

        if (clienteExiste) {
            throw new RuntimeException("Cliente '" + nomeCliente + "' já está na mesa.");
        }

        // 9. Criar novo cliente na comanda
        ClienteComanda novoCliente = new ClienteComanda();
        novoCliente.setNome(nomeCliente);
        novoCliente.setComanda(comanda);
        novoCliente.setValorTotal(BigDecimal.ZERO);
        novoCliente.setPago(false);
        novoCliente = clienteComandaRepository.save(novoCliente);

        System.out.println("✅ Cliente '" + nomeCliente + "' adicionado à comanda " + comandaId);

        return novoCliente;
    }

    // ========== REMOVER ITEM COM JUSTIFICATIVA ==========

    @Transactional
    public RemoverItemResponse removerItemComJustificativa(
            Long comandaId,
            Long clienteComandaId,
            Long itemId,
            String justificativa,
            String removidoPor,
            Long companyId
    ) {
        // 1. Buscar o item
        ComandaItem item = comandaItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado"));

        // 2. Verificar se pertence à comanda
        if (!item.getComanda().getId().equals(comandaId)) {
            throw new RuntimeException("Item não pertence a esta comanda");
        }

        // 3. Verificar se pertence ao cliente
        if (item.getClienteComanda() == null || !item.getClienteComanda().getId().equals(clienteComandaId)) {
            throw new RuntimeException("Item não pertence a este cliente");
        }

        // 4. Verificar se a comanda está aberta
        Comanda comanda = item.getComanda();
        if (comanda.getStatus() != StatusComanda.ABERTA) {
            throw new RuntimeException("Comanda já foi fechada ou paga. Não é possível remover itens.");
        }

        // 5. Verificar se a empresa é a mesma
        if (!comanda.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Item não pertence à sua empresa");
        }

        // 6. Guardar informações do item para resposta
        String produtoNome = item.getProduto().getNome();
        Integer quantidadeRemovida = item.getQuantidade();
        BigDecimal precoUnitario = item.getPrecoUnitario();
        BigDecimal precoTotalRemovido = item.getPrecoTotal();
        String clienteNome = item.getClienteComanda().getNome();

        // 7. Devolver produto ao estoque
        Produto produto = item.getProduto();
        produto.setQuantidade(produto.getQuantidade() + item.getQuantidade());
        produtoRepository.save(produto);

        // 8. Remover o item da comanda
        comandaItemRepository.delete(item);

        // 9. Atualizar valor do cliente
        ClienteComanda cliente = item.getClienteComanda();
        cliente.setValorTotal(cliente.getValorTotal().subtract(precoTotalRemovido));
        clienteComandaRepository.save(cliente);

        // 10. Atualizar valor da comanda
        comanda.setValorTotal(comanda.getValorTotal().subtract(precoTotalRemovido));
        comandaRepository.save(comanda);

        // 11. Construir resposta
        return RemoverItemResponse.builder()
                .message("✅ Item removido com sucesso!")
                .produtoNome(produtoNome)
                .quantidadeRemovida(quantidadeRemovida)
                .precoUnitario(precoUnitario)
                .precoTotal(precoTotalRemovido)
                .clienteNome(clienteNome)
                .justificativa(justificativa)
                .removidoPor(removidoPor)
                .build();
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
            List<Comanda> comandas = comandaRepository.findByIdentificadorAndTipoAndCompanyId(
                    String.valueOf(mesa.getNumeroMesa()),
                    TipoComanda.MESA,
                    companyId
            );

            Comanda comandaAtiva = comandas.stream()
                    .filter(c -> c.getStatus() == StatusComanda.ABERTA)
                    .findFirst()
                    .orElse(null);

            ComandaInfoDTO comandaInfo = null;

            if (comandaAtiva != null) {
                List<ClienteComanda> clientes = clienteComandaRepository.findByComandaId(comandaAtiva.getId());

                List<ClienteConsumoDTO> clientesConsumo = clientes.stream().map(cliente -> {
                    List<ComandaItem> itensCliente = comandaItemRepository.findByComandaId(comandaAtiva.getId());

                    List<ItemConsumoDTO> itensConsumo = itensCliente.stream()
                            .filter(item -> item.getClienteComanda() != null &&
                                    item.getClienteComanda().getId().equals(cliente.getId()))
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

        List<Comanda> comandas = comandaRepository.findByIdentificadorAndTipoAndCompanyId(
                String.valueOf(mesa.getNumeroMesa()),
                TipoComanda.MESA,
                companyId
        );

        Comanda comandaAberta = comandas.stream()
                .filter(c -> c.getStatus() == StatusComanda.ABERTA)
                .findFirst()
                .orElse(null);

        if (comandaAberta != null) {
            // 1. Remover pagamentos
            List<Pagamento> pagamentos = pagamentoRepository.findByComandaId(comandaAberta.getId());
            if (!pagamentos.isEmpty()) {
                pagamentoRepository.deleteAll(pagamentos);
            }

            // 2. Remover itens
            List<ComandaItem> itens = comandaItemRepository.findByComandaId(comandaAberta.getId());
            if (!itens.isEmpty()) {
                comandaItemRepository.deleteAll(itens);
            }

            // 3. Remover clientes
            List<ClienteComanda> clientes = clienteComandaRepository.findByComandaId(comandaAberta.getId());
            if (!clientes.isEmpty()) {
                clienteComandaRepository.deleteAll(clientes);
                System.out.println(clientes.size() + " clientes removidos da mesa " + mesa.getNumeroMesa());
            }

            comandaAberta.setStatus(StatusComanda.FECHADA);
            comandaAberta.setDataFechamento(LocalDateTime.now());
            comandaRepository.save(comandaAberta);
        }

        mesa.setIsOcupada(false);
        mesaRepository.save(mesa);
    }

    @Transactional
    public void limparMesaCompleta(Long mesaId, Long companyId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        if (!mesa.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Mesa não pertence à sua empresa");
        }

        List<Comanda> comandas = comandaRepository.findByIdentificadorAndTipoAndCompanyId(
                String.valueOf(mesa.getNumeroMesa()),
                TipoComanda.MESA,
                companyId
        );

        for (Comanda comanda : comandas) {
            if (comanda.getStatus() == StatusComanda.ABERTA) {
                List<ClienteComanda> clientes = clienteComandaRepository.findByComandaId(comanda.getId());
                if (!clientes.isEmpty()) {
                    clienteComandaRepository.deleteAll(clientes);
                }

                List<ComandaItem> itens = comandaItemRepository.findByComandaId(comanda.getId());
                if (!itens.isEmpty()) {
                    comandaItemRepository.deleteAll(itens);
                }

                comanda.setStatus(StatusComanda.FECHADA);
                comanda.setValorTotal(BigDecimal.ZERO);
                comanda.setDataFechamento(LocalDateTime.now());
                comandaRepository.save(comanda);
            }
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

        if (mesa.getIsOcupada()) {
            throw new RuntimeException("Não é possível deletar uma mesa ocupada. Libere a mesa primeiro.");
        }

        List<Comanda> comandas = comandaRepository.findByIdentificadorAndTipoAndCompanyId(
                String.valueOf(mesa.getNumeroMesa()),
                TipoComanda.MESA,
                companyId
        );

        if (!comandas.isEmpty()) {
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
        // Validar se contém apenas números
        if (!numeroPulseira.matches("^[0-9]+$")) {
            throw new RuntimeException("Número da pulseira deve conter apenas números");
        }

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

    @Transactional
    public PulseiraProdutoResponse adicionarProdutoNaPulseira(String numeroPulseira, Long produtoId, Integer quantidade, Long companyId) {

        // 1. Validar se a pulseira existe e está ativa
        Pulseira pulseira = pulseiraRepository.findByNumeroPulseiraAndCompanyId(numeroPulseira, companyId)
                .orElseThrow(() -> new RuntimeException("Pulseira não encontrada ou inativa"));

        if (!pulseira.getIsAtivo()) {
            throw new RuntimeException("Pulseira está inativa");
        }

        // 2. Buscar produto
        Produto produto = produtoRepository.findByIdAndCompanyId(produtoId, companyId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (produto.getQuantidade() < quantidade) {
            throw new RuntimeException("Estoque insuficiente. Disponível: " + produto.getQuantidade());
        }

        // 3. Buscar comanda aberta da pulseira
        List<Comanda> comandas = comandaRepository.findByIdentificadorAndTipoAndCompanyId(
                numeroPulseira, TipoComanda.PULSEIRA, companyId);

        Comanda comanda = comandas.stream()
                .filter(c -> c.getStatus() == StatusComanda.ABERTA)
                .findFirst()
                .orElse(null);

        // 4. Se não tiver comanda aberta, criar uma
        if (comanda == null) {
            String numeroComanda = "PULSEIRA-" + numeroPulseira + "-" +
                    UUID.randomUUID().toString().substring(0, 4).toUpperCase();

            // 🔥 BUSCAR A EMPRESA
            Company company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

            comanda = new Comanda();
            comanda.setNumeroComanda(numeroComanda);
            comanda.setTipoComanda(TipoComanda.PULSEIRA);
            comanda.setIdentificadorComanda(numeroPulseira);
            comanda.setCompany(company);
            comanda.setStatus(StatusComanda.ABERTA);
            comanda.setValorTotal(BigDecimal.ZERO);
            comanda = comandaRepository.save(comanda);

            // Criar cliente na comanda
            ClienteComanda cliente = new ClienteComanda();
            cliente.setNome(pulseira.getNomeCliente() != null ? pulseira.getNomeCliente() : "Cliente Pulseira " + numeroPulseira);
            cliente.setComanda(comanda);
            cliente.setValorTotal(BigDecimal.ZERO);
            cliente.setPago(false);
            cliente = clienteComandaRepository.save(cliente);
        }

        // 5. Buscar cliente da comanda
        List<ClienteComanda> clientes = clienteComandaRepository.findByComandaId(comanda.getId());
        ClienteComanda cliente = clientes.stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado na comanda"));

        // 6. Verificar se o cliente já tem esse produto (para acumular)
        List<ComandaItem> itensExistentes = comandaItemRepository.findByComandaId(comanda.getId());
        ComandaItem itemExistente = itensExistentes.stream()
                .filter(item -> item.getClienteComanda() != null &&
                        item.getClienteComanda().getId().equals(cliente.getId()) &&
                        item.getProduto().getId().equals(produtoId))
                .findFirst()
                .orElse(null);

        BigDecimal precoTotal;
        ComandaItem item;

        if (itemExistente != null) {
            // Produto já existe - atualizar quantidade
            int novaQuantidade = itemExistente.getQuantidade() + quantidade;
            itemExistente.setQuantidade(novaQuantidade);
            itemExistente.setPrecoTotal(itemExistente.getPrecoUnitario().multiply(BigDecimal.valueOf(novaQuantidade)));
            item = comandaItemRepository.save(itemExistente);
            precoTotal = item.getPrecoUnitario().multiply(BigDecimal.valueOf(quantidade));

            // Atualizar valor do cliente
            cliente.setValorTotal(cliente.getValorTotal().add(precoTotal));

            // Atualizar valor da comanda
            comanda.setValorTotal(comanda.getValorTotal().add(precoTotal));
        } else {
            // Produto novo - criar item
            produto.setQuantidade(produto.getQuantidade() - quantidade);
            produtoRepository.save(produto);

            item = new ComandaItem();
            item.setComanda(comanda);
            item.setProduto(produto);
            item.setClienteComanda(cliente);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(produto.getPreco());
            item.calcularTotal();
            item = comandaItemRepository.save(item);

            precoTotal = item.getPrecoTotal();

            cliente.setValorTotal(cliente.getValorTotal().add(precoTotal));
            comanda.setValorTotal(comanda.getValorTotal().add(precoTotal));
        }

        clienteComandaRepository.save(cliente);
        comandaRepository.save(comanda);

        // 7. Retornar resposta
        return PulseiraProdutoResponse.builder()
                .message("✅ Produto adicionado à pulseira com sucesso!")
                .numeroPulseira(numeroPulseira)
                .nomeCliente(pulseira.getNomeCliente())
                .produtoNome(produto.getNome())
                .quantidade(quantidade)
                .precoTotal(precoTotal)
                .novoSaldo(cliente.getValorTotal())
                .build();
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

        List<ComandaItem> itensExistentes = comandaItemRepository.findByComandaId(comandaId);
        ComandaItem itemExistente = itensExistentes.stream()
                .filter(item -> item.getClienteComanda() != null &&
                        item.getClienteComanda().getId().equals(clienteComandaId) &&
                        item.getProduto().getId().equals(produtoId))
                .findFirst()
                .orElse(null);

        ComandaItem item;

        if (itemExistente != null) {
            int novaQuantidade = itemExistente.getQuantidade() + quantidade;
            itemExistente.setQuantidade(novaQuantidade);
            itemExistente.setPrecoTotal(itemExistente.getPrecoUnitario().multiply(BigDecimal.valueOf(novaQuantidade)));
            item = comandaItemRepository.save(itemExistente);

            BigDecimal novoValorCliente = clienteComanda.getValorTotal().add(
                    itemExistente.getPrecoUnitario().multiply(BigDecimal.valueOf(quantidade))
            );
            clienteComanda.setValorTotal(novoValorCliente);

            BigDecimal novoValorComanda = comanda.getValorTotal().add(
                    itemExistente.getPrecoUnitario().multiply(BigDecimal.valueOf(quantidade))
            );
            comanda.setValorTotal(novoValorComanda);
        } else {
            produto.setQuantidade(produto.getQuantidade() - quantidade);
            produtoRepository.save(produto);

            item = new ComandaItem();
            item.setComanda(comanda);
            item.setProduto(produto);
            item.setClienteComanda(clienteComanda);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(produto.getPreco());
            item.calcularTotal();

            clienteComanda.setValorTotal(clienteComanda.getValorTotal().add(item.getPrecoTotal()));
            comanda.setValorTotal(comanda.getValorTotal().add(item.getPrecoTotal()));

            item = comandaItemRepository.save(item);
        }
        clienteComandaRepository.save(clienteComanda);
        comandaRepository.save(comanda);

        return item;
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
            comanda.setDataFechamento(LocalDateTime.now());
            comandaRepository.save(comanda);

            if (comanda.getTipoComanda() == TipoComanda.MESA) {
                liberarMesaPorComanda(comanda, companyId);
            }

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

    @Transactional
    public void forcarLimpezaMesa(Long mesaId, Long companyId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa não encontrada"));

        if (!mesa.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Mesa não pertence à sua empresa");
        }

        List<Comanda> comandas = comandaRepository.findByIdentificadorAndTipoAndCompanyId(
                String.valueOf(mesa.getNumeroMesa()),
                TipoComanda.MESA,
                companyId
        );

        for (Comanda comanda : comandas) {
            // 1. Remover itens
            List<ComandaItem> itens = comandaItemRepository.findByComandaId(comanda.getId());
            if (!itens.isEmpty()) {
                comandaItemRepository.deleteAll(itens);
            }

            // 2. Remover clientes
            List<ClienteComanda> clientes = clienteComandaRepository.findByComandaId(comanda.getId());
            if (!clientes.isEmpty()) {
                clienteComandaRepository.deleteAll(clientes);
            }

            // 3. Deletar a comanda
            comandaRepository.delete(comanda);
        }

        // 4. Liberar a mesa
        mesa.setIsOcupada(false);
        mesaRepository.save(mesa);
    }
}