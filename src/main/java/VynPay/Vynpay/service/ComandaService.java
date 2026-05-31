package VynPay.Vynpay.service;

import VynPay.Vynpay.enun.FormaPagamento;
import VynPay.Vynpay.enun.StatusComanda;
import VynPay.Vynpay.model.*;
import VynPay.Vynpay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ComandaService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ComandaRepository comandaRepository;

    @Autowired
    private ComandaItemRepository comandaItemRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

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

    public Cliente buscarClientePorId(Long clienteId, Long companyId) {
        return clienteRepository.findByIdAndCompanyId(clienteId, companyId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    public List<Cliente> listarClientesComComandaAtiva(Long companyId) {
        return clienteRepository.findByCompanyIdAndComandaAtivaTrue(companyId);
    }

    // ========== COMANDAS ==========

    @Transactional
    public Comanda abrirComanda(Company company, Long clienteId) {
        Cliente cliente = buscarClientePorId(clienteId, company.getId());

        // Verificar se cliente já tem comanda ativa
        if (cliente.getComandaAtiva()) {
            throw new RuntimeException("Cliente já possui uma comanda ativa");
        }

        // Gerar número único da comanda
        String numeroComanda = "COM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Comanda comanda = new Comanda();
        comanda.setNumeroComanda(numeroComanda);
        comanda.setCliente(cliente);
        comanda.setCompany(company);
        comanda.setStatus(StatusComanda.ABERTA);
        comanda.setValorTotal(BigDecimal.ZERO);

        cliente.setComandaAtiva(true);
        clienteRepository.save(cliente);

        return comandaRepository.save(comanda);
    }

    public Comanda buscarComandaAtivaPorCliente(Long clienteId, Long companyId) {
        return comandaRepository.findByClienteIdAndStatus(clienteId, StatusComanda.ABERTA)
                .orElseThrow(() -> new RuntimeException("Nenhuma comanda ativa encontrada para este cliente"));
    }

    public Comanda buscarComandaPorNumero(String numeroComanda, Long companyId) {
        return comandaRepository.findByNumeroComandaAndCompanyId(numeroComanda, companyId)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));
    }

    // ========== ITENS DA COMANDA ==========

    @Transactional
    public ComandaItem adicionarProdutoNaComanda(Long comandaId, Long produtoId, Integer quantidade, Long companyId) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new RuntimeException("Comanda não encontrada"));

        if (!comanda.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Comanda não pertence à sua empresa");
        }

        if (comanda.getStatus() != StatusComanda.ABERTA) {
            throw new RuntimeException("Comanda já foi fechada ou paga");
        }

        Produto produto = produtoRepository.findByIdAndCompanyId(produtoId, companyId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Verificar se tem estoque
        if (produto.getQuantidade() < quantidade) {
            throw new RuntimeException("Estoque insuficiente. Disponível: " + produto.getQuantidade());
        }

        // Atualizar estoque
        produto.setQuantidade(produto.getQuantidade() - quantidade);
        produtoRepository.save(produto);

        // Criar item da comanda
        ComandaItem item = new ComandaItem();
        item.setComanda(comanda);
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setPrecoUnitario(produto.getPreco());
        item.calcularTotal();

        // Atualizar valor total da comanda
        comanda.setValorTotal(comanda.getValorTotal().add(item.getPrecoTotal()));
        comandaRepository.save(comanda);

        return comandaItemRepository.save(item);
    }

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

        // Devolver ao estoque
        Produto produto = item.getProduto();
        produto.setQuantidade(produto.getQuantidade() + item.getQuantidade());
        produtoRepository.save(produto);

        // Atualizar valor total da comanda
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

        // Registrar pagamento
        Pagamento pagamento = new Pagamento();
        pagamento.setComanda(comanda);
        pagamento.setValorPago(valorPago);
        pagamento.setFormaPagamento(formaPagamento);

        // Atualizar comanda
        comanda.setStatus(StatusComanda.PAGA);

        // Limpar comanda ativa do cliente
        Cliente cliente = comanda.getCliente();
        cliente.setComandaAtiva(false);
        clienteRepository.save(cliente);

        comandaRepository.save(comanda);

        return pagamentoRepository.save(pagamento);
    }

    public List<Comanda> listarComandasPorStatus(Long companyId, StatusComanda status) {
        return comandaRepository.findByCompanyIdAndStatus(companyId, status);
    }

    public BigDecimal calcularTotalDoDia(Long companyId) {
        List<Pagamento> pagamentos = pagamentoRepository.findAll();
        return pagamentos.stream()
                .filter(p -> p.getDataPagamento().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .filter(p -> p.getComanda().getCompany().getId().equals(companyId))
                .map(Pagamento::getValorPago)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}