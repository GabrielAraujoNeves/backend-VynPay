package VynPay.Vynpay.service;

import VynPay.Vynpay.dto.request.CategoriaEstoqueRequest;
import VynPay.Vynpay.dto.request.CategoriaProdutoRequest;
import VynPay.Vynpay.dto.request.CategoriaUpdateRequest;
import VynPay.Vynpay.dto.response.CategoriaEstoqueResponse;
import VynPay.Vynpay.dto.response.CategoriaProdutoResponse;
import VynPay.Vynpay.model.Categoria;
import VynPay.Vynpay.model.Categoria.TipoCategoria;
import VynPay.Vynpay.model.Company;
import VynPay.Vynpay.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // ========== CATEGORIAS DE PRODUTOS ==========

    @Transactional
    public CategoriaProdutoResponse criarCategoriaProduto(Company company, CategoriaProdutoRequest request) {
        if (categoriaRepository.findByNomeAndCompanyId(request.getNome(), company.getId()).isPresent()) {
            throw new RuntimeException("Categoria de produto já existe");
        }

        Categoria categoria = new Categoria();
        categoria.setCompany(company);
        categoria.setNome(request.getNome());
        categoria.setTipoCategoria(TipoCategoria.PRODUTO);
        categoria.setIsAtivo(true);

        Categoria saved = categoriaRepository.save(categoria);

        CategoriaProdutoResponse response = new CategoriaProdutoResponse();
        response.setId(saved.getId());
        response.setNome(saved.getNome());
        return response;
    }

    public List<CategoriaProdutoResponse> listarCategoriasProduto(Long companyId) {
        List<Categoria> todas = categoriaRepository.findByCompanyId(companyId);

        List<Categoria> categorias = todas.stream()
                .filter(c -> c.getTipoCategoria() == null || c.getTipoCategoria() == TipoCategoria.PRODUTO)
                .collect(Collectors.toList());

        return categorias.stream()
                .map(this::toProdutoResponse)
                .collect(Collectors.toList());
    }

    public List<CategoriaProdutoResponse> listarCategoriasProdutoAtivas(Long companyId) {
        List<Categoria> todas = categoriaRepository.findByCompanyIdAndIsAtivoTrue(companyId);

        List<Categoria> categorias = todas.stream()
                .filter(c -> c.getTipoCategoria() == null || c.getTipoCategoria() == TipoCategoria.PRODUTO)
                .collect(Collectors.toList());

        return categorias.stream()
                .map(this::toProdutoResponse)
                .collect(Collectors.toList());
    }

    public List<CategoriaProdutoResponse> buscarCategoriasProdutoPorNome(Long companyId, String nome) {
        List<Categoria> todas = categoriaRepository.findByCompanyIdAndNomeContainingIgnoreCase(companyId, nome);
        return todas.stream()
                .filter(c -> c.getTipoCategoria() == null || c.getTipoCategoria() == TipoCategoria.PRODUTO)
                .map(this::toProdutoResponse)
                .collect(Collectors.toList());
    }

    // ========== CATEGORIAS DE ESTOQUE ==========

    @Transactional
    public CategoriaEstoqueResponse criarCategoriaEstoque(Company company, CategoriaEstoqueRequest request) {
        if (categoriaRepository.findByNomeAndCompanyId(request.getNome(), company.getId()).isPresent()) {
            throw new RuntimeException("Categoria de estoque já existe");
        }

        Categoria categoria = new Categoria();
        categoria.setCompany(company);
        categoria.setNome(request.getNome());
        categoria.setTipoCategoria(TipoCategoria.ESTOQUE);
        categoria.setIsAtivo(true);

        Categoria saved = categoriaRepository.save(categoria);

        CategoriaEstoqueResponse response = new CategoriaEstoqueResponse();
        response.setId(saved.getId());
        response.setNome(saved.getNome());
        return response;
    }

    public List<CategoriaEstoqueResponse> listarCategoriasEstoque(Long companyId) {
        List<Categoria> categorias = categoriaRepository.findByCompanyIdAndTipoCategoria(companyId, TipoCategoria.ESTOQUE);
        return categorias.stream()
                .map(this::toEstoqueResponse)
                .collect(Collectors.toList());
    }

    public List<CategoriaEstoqueResponse> listarCategoriasEstoqueAtivas(Long companyId) {
        List<Categoria> categorias = categoriaRepository.findByCompanyIdAndTipoCategoriaAndIsAtivoTrue(companyId, TipoCategoria.ESTOQUE);
        return categorias.stream()
                .map(this::toEstoqueResponse)
                .collect(Collectors.toList());
    }

    public List<CategoriaEstoqueResponse> buscarCategoriasEstoquePorNome(Long companyId, String nome) {
        List<Categoria> categorias = categoriaRepository.findByCompanyIdAndNomeContainingIgnoreCase(companyId, nome);
        return categorias.stream()
                .filter(c -> c.getTipoCategoria() == TipoCategoria.ESTOQUE)
                .map(this::toEstoqueResponse)
                .collect(Collectors.toList());
    }

    // ========== EDITAR CATEGORIAS (PRODUTO E ESTOQUE) ==========

    @Transactional
    public CategoriaProdutoResponse editarCategoriaProduto(Long id, Long companyId, CategoriaUpdateRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!categoria.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Categoria não pertence a esta empresa");
        }

        // Aceita NULL como PRODUTO
        if (categoria.getTipoCategoria() != null && categoria.getTipoCategoria() != TipoCategoria.PRODUTO) {
            throw new RuntimeException("Esta categoria não é do tipo PRODUTO");
        }

        // Verificar se o novo nome já existe
        if (request.getNome() != null && !request.getNome().isEmpty()) {
            if (categoriaRepository.findByNomeAndCompanyId(request.getNome(), companyId).isPresent() &&
                    !categoria.getNome().equals(request.getNome())) {
                throw new RuntimeException("Já existe uma categoria com este nome");
            }
            categoria.setNome(request.getNome());

            // Se era NULL, atualiza para PRODUTO
            if (categoria.getTipoCategoria() == null) {
                categoria.setTipoCategoria(TipoCategoria.PRODUTO);
            }
        }

        Categoria saved = categoriaRepository.save(categoria);
        return toProdutoResponse(saved);
    }

    @Transactional
    public CategoriaEstoqueResponse editarCategoriaEstoque(Long id, Long companyId, CategoriaUpdateRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!categoria.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Categoria não pertence a esta empresa");
        }

        if (categoria.getTipoCategoria() != TipoCategoria.ESTOQUE) {
            throw new RuntimeException("Esta categoria não é do tipo ESTOQUE");
        }

        if (request.getNome() != null && !request.getNome().isEmpty()) {
            if (categoriaRepository.findByNomeAndCompanyId(request.getNome(), companyId).isPresent() &&
                    !categoria.getNome().equals(request.getNome())) {
                throw new RuntimeException("Já existe uma categoria com este nome");
            }
            categoria.setNome(request.getNome());
        }

        Categoria saved = categoriaRepository.save(categoria);
        return toEstoqueResponse(saved);
    }

    // ========== MÉTODO GENÉRICO PARA DELETAR ==========

    @Transactional
    public void deletarCategoria(Long id, Long companyId) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!categoria.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Categoria não pertence a esta empresa");
        }

        // Verificar se tem produtos vinculados
        if (categoria.getProdutos() != null && !categoria.getProdutos().isEmpty()) {
            throw new RuntimeException("Não é possível deletar categoria com produtos vinculados. Mova ou delete os produtos primeiro.");
        }

        // Verificar se tem itens de estoque vinculados
        if (categoria.getItensEstoque() != null && !categoria.getItensEstoque().isEmpty()) {
            throw new RuntimeException("Não é possível deletar categoria com itens de estoque vinculados.");
        }

        categoriaRepository.delete(categoria);
    }

    // ========== DELETAR CATEGORIAS (PRODUTO - ACEITA NULL) ==========

    @Transactional
    public void deletarCategoriaProduto(Long id, Long companyId) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!categoria.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Categoria não pertence a esta empresa");
        }

        // Aceita NULL como PRODUTO
        if (categoria.getTipoCategoria() != null && categoria.getTipoCategoria() != TipoCategoria.PRODUTO) {
            throw new RuntimeException("Esta categoria não é do tipo PRODUTO");
        }

        // Verificar se tem produtos vinculados
        if (categoria.getProdutos() != null && !categoria.getProdutos().isEmpty()) {
            throw new RuntimeException("Não é possível deletar categoria com produtos vinculados. Mova ou delete os produtos primeiro.");
        }

        categoriaRepository.delete(categoria);
    }

    @Transactional
    public void deletarCategoriaEstoque(Long id, Long companyId) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (!categoria.getCompany().getId().equals(companyId)) {
            throw new RuntimeException("Categoria não pertence a esta empresa");
        }

        if (categoria.getTipoCategoria() != TipoCategoria.ESTOQUE) {
            throw new RuntimeException("Esta categoria não é do tipo ESTOQUE");
        }

        if (categoria.getItensEstoque() != null && !categoria.getItensEstoque().isEmpty()) {
            throw new RuntimeException("Não é possível deletar categoria com itens de estoque vinculados.");
        }

        categoriaRepository.delete(categoria);
    }

    // ========== MÉTODOS AUXILIARES ==========

    private CategoriaProdutoResponse toProdutoResponse(Categoria categoria) {
        CategoriaProdutoResponse response = new CategoriaProdutoResponse();
        response.setId(categoria.getId());
        response.setNome(categoria.getNome());
        return response;
    }

    private CategoriaEstoqueResponse toEstoqueResponse(Categoria categoria) {
        CategoriaEstoqueResponse response = new CategoriaEstoqueResponse();
        response.setId(categoria.getId());
        response.setNome(categoria.getNome());
        return response;
    }
}