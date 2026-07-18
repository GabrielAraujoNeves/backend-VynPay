package VynPay.Vynpay.service;

import VynPay.Vynpay.dto.request.EstoqueRequest;
import VynPay.Vynpay.dto.response.EstoqueResponse;
import VynPay.Vynpay.model.Categoria;
import VynPay.Vynpay.model.Company;
import VynPay.Vynpay.model.Estoque;
import VynPay.Vynpay.model.Produto;
import VynPay.Vynpay.repository.CategoriaRepository;
import VynPay.Vynpay.repository.EstoqueRepository;
import VynPay.Vynpay.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstoqueService {

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Criar novo item no estoque
    @Transactional
    public Estoque criarItemEstoque(Company company, EstoqueRequest request) {
        Estoque estoque = new Estoque();
        estoque.setCompany(company);
        estoque.setNomeProduto(request.getNomeProduto());
        estoque.setQuantidade(request.getQuantidade() != null ? request.getQuantidade() : 0);
        estoque.setUnidadeMedida(request.getUnidadeMedida());
        estoque.setPesoVolume(request.getPesoVolume());
        estoque.setPrecoUnitario(request.getPrecoUnitario());
        estoque.setPrecoCompra(request.getPrecoCompra());
        estoque.setEstoqueMinimo(request.getEstoqueMinimo());
        estoque.setEstoqueMaximo(request.getEstoqueMaximo());
        estoque.setLocalizacao(request.getLocalizacao());
        estoque.setFornecedor(request.getFornecedor());
        estoque.setDataValidade(request.getDataValidade());
        estoque.setObservacoes(request.getObservacoes());

        // Vincular com categoria
        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

            if (!categoria.getCompany().getId().equals(company.getId())) {
                throw new RuntimeException("Categoria não pertence a esta empresa");
            }
            estoque.setCategoria(categoria);
        }

        // Vincular com produto existente se informado
        if (request.getProdutoId() != null) {
            Produto produto = produtoRepository.findByIdAndCompanyId(request.getProdutoId(), company.getId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
            estoque.setProduto(produto);

            // Se não tiver nome, pegar do produto
            if (request.getNomeProduto() == null || request.getNomeProduto().isEmpty()) {
                estoque.setNomeProduto(produto.getNome());
            }
        }

        return estoqueRepository.save(estoque);
    }

    // Adicionar quantidade ao estoque
    @Transactional
    public Estoque adicionarQuantidade(Long id, Long companyId, Integer quantidade) {
        Estoque item = estoqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de estoque não encontrado"));

        if (!item.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Item não pertence a esta empresa");
        }

        item.adicionarQuantidade(quantidade);
        return estoqueRepository.save(item);
    }

    // Remover quantidade do estoque
    @Transactional
    public Estoque removerQuantidade(Long id, Long companyId, Integer quantidade) {
        Estoque item = estoqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de estoque não encontrado"));

        if (!item.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Item não pertence a esta empresa");
        }

        item.removerQuantidade(quantidade);
        return estoqueRepository.save(item);
    }

    // Listar todos do estoque da empresa
    public List<EstoqueResponse> listarEstoque(Long companyId) {
        List<Estoque> estoqueList = estoqueRepository.findByCompanyId(companyId);
        return estoqueList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Buscar por nome
    public List<EstoqueResponse> buscarPorNome(Long companyId, String nome) {
        List<Estoque> estoqueList = estoqueRepository.findByCompanyIdAndNomeProdutoContainingIgnoreCase(companyId, nome);
        return estoqueList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Buscar por categoria
    public List<EstoqueResponse> buscarPorCategoria(Long companyId, Long categoriaId) {
        List<Estoque> estoqueList = estoqueRepository.findByCompanyIdAndCategoriaId(companyId, categoriaId);
        return estoqueList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Buscar itens com estoque baixo
    public List<EstoqueResponse> buscarEstoqueBaixo(Long companyId) {
        List<Estoque> estoqueList = estoqueRepository.findProdutosComEstoqueBaixo(companyId);
        return estoqueList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Buscar produtos vencidos
    public List<EstoqueResponse> buscarVencidos(Long companyId) {
        List<Estoque> estoqueList = estoqueRepository.findProdutosVencidos(companyId, LocalDateTime.now());
        return estoqueList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Buscar produtos próximos a vencer (30 dias)
    public List<EstoqueResponse> buscarProximosVencer(Long companyId) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime trintaDias = agora.plusDays(30);
        List<Estoque> estoqueList = estoqueRepository.findProdutosProximosVencer(companyId, agora, trintaDias);
        return estoqueList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Buscar por fornecedor
    public List<EstoqueResponse> buscarPorFornecedor(Long companyId, String fornecedor) {
        List<Estoque> estoqueList = estoqueRepository.findByCompanyIdAndFornecedorContainingIgnoreCase(companyId, fornecedor);
        return estoqueList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // Buscar item por ID
    public EstoqueResponse buscarPorId(Long id, Long companyId) {
        Estoque item = estoqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de estoque não encontrado"));

        if (!item.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Item não pertence a esta empresa");
        }

        return convertToResponse(item);
    }

    // Atualizar item do estoque
    @Transactional
    public Estoque atualizarItem(Long id, Long companyId, EstoqueRequest request) {
        Estoque item = estoqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de estoque não encontrado"));

        if (!item.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Item não pertence a esta empresa");
        }

        // Atualizar apenas campos que vieram no request
        if (request.getNomeProduto() != null) item.setNomeProduto(request.getNomeProduto());
        if (request.getQuantidade() != null) item.setQuantidade(request.getQuantidade());
        if (request.getUnidadeMedida() != null) item.setUnidadeMedida(request.getUnidadeMedida());
        if (request.getPesoVolume() != null) item.setPesoVolume(request.getPesoVolume());
        if (request.getPrecoUnitario() != null) item.setPrecoUnitario(request.getPrecoUnitario());
        if (request.getPrecoCompra() != null) item.setPrecoCompra(request.getPrecoCompra());
        if (request.getEstoqueMinimo() != null) item.setEstoqueMinimo(request.getEstoqueMinimo());
        if (request.getEstoqueMaximo() != null) item.setEstoqueMaximo(request.getEstoqueMaximo());
        if (request.getLocalizacao() != null) item.setLocalizacao(request.getLocalizacao());
        if (request.getFornecedor() != null) item.setFornecedor(request.getFornecedor());
        if (request.getDataValidade() != null) item.setDataValidade(request.getDataValidade());
        if (request.getObservacoes() != null) item.setObservacoes(request.getObservacoes());

        // Atualizar categoria se informado
        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

            if (!categoria.getCompany().getId().equals(companyId)) {
                throw new RuntimeException("Categoria não pertence a esta empresa");
            }
            item.setCategoria(categoria);
        }

        return estoqueRepository.save(item);
    }

    // Deletar item
    @Transactional
    public void deletarItem(Long id, Long companyId) {
        Estoque item = estoqueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item de estoque não encontrado"));

        if (!item.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Item não pertence a esta empresa");
        }

        estoqueRepository.delete(item);
    }

    // Resumo do estoque
    public List<Object[]> getResumoPorCategoria(Long companyId) {
        return estoqueRepository.sumQuantidadePorCategoria(companyId);
    }

    // Valor total do estoque
    public Double getValorTotalEstoque(Long companyId) {
        return estoqueRepository.calcularValorTotalEstoque(companyId);
    }

    // Converter para Response
    private EstoqueResponse convertToResponse(Estoque estoque) {
        EstoqueResponse response = new EstoqueResponse();
        response.setId(estoque.getId());
        response.setNomeProduto(estoque.getNomeProduto());

        // Adicionar informações da categoria (SEM TIPO)
        if (estoque.getCategoria() != null) {
            response.setCategoriaId(estoque.getCategoria().getId());
            response.setCategoriaNome(estoque.getCategoria().getNome());
            // REMOVIDO: response.setCategoriaTipo(estoque.getCategoria().getTipoCategoria());
        }

        response.setQuantidade(estoque.getQuantidade());
        response.setUnidadeMedida(estoque.getUnidadeMedida());
        response.setPesoVolume(estoque.getPesoVolume());
        response.setPrecoUnitario(estoque.getPrecoUnitario());
        response.setPrecoCompra(estoque.getPrecoCompra());
        response.setEstoqueMinimo(estoque.getEstoqueMinimo());
        response.setEstoqueMaximo(estoque.getEstoqueMaximo());
        response.setLocalizacao(estoque.getLocalizacao());
        response.setFornecedor(estoque.getFornecedor());
        response.setDataValidade(estoque.getDataValidade());
        response.setDataCadastro(estoque.getDataCadastro());
        response.setDataAtualizacao(estoque.getDataAtualizacao());
        response.setObservacoes(estoque.getObservacoes());
        response.setValorTotal(estoque.getValorTotalEstoque());
        response.setIsEstoqueBaixo(estoque.isEstoqueBaixo());
        response.setIsEstoqueAlto(estoque.isEstoqueAlto());
        response.setIsVencido(estoque.isVencido());
        response.setIsProximoVencer(estoque.isProximoVencer());
        return response;
    }
}