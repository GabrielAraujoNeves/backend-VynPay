package VynPay.Vynpay.service;

import VynPay.Vynpay.model.CategoriaProduto;
import VynPay.Vynpay.model.Company;
import VynPay.Vynpay.model.Produto;
import VynPay.Vynpay.repository.CategoriaRepository;
import VynPay.Vynpay.repository.ProdutoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // aqui vai adiciona a categoria no sistema daquele emprsa
    @Transactional
    public CategoriaProduto criarCategoria (Company company, String nome, String descricao) {
      if (categoriaRepository.findByNomeAndCompanyId(nome, company.getId()).isPresent()) {
          throw new RuntimeException("Categoria ja existe nesta empresa");
      }

        CategoriaProduto categoria = new CategoriaProduto();
        categoria.setNome(nome);
        categoria.setDescricao(descricao);
        categoria.setCompany(company);

        return categoriaRepository.save(categoria);
    }

    // aqui vai lista as categorias daquela empresa
    public List<CategoriaProduto> listarCategorias(Long companyId) {
       return categoriaRepository.findByCompanyId(companyId);
    }

    //lista a categoria por Id
    public CategoriaProduto buscarCategoriaPorId(Long categoriaId, Long companyId) {
       return categoriaRepository.findByIdAndCompanyId(categoriaId, companyId)
               .orElseThrow(() -> new RuntimeException("Categoria nao encontrada"));
    }


    //aqui vau atualiza a categoria
    @Transactional
    public CategoriaProduto atualizarCategoria(Long categoriaId, Long companyId, String nome, String descricao) {

       CategoriaProduto categoria = buscarCategoriaPorId(categoriaId, companyId);

       if (nome != null && !nome.isEmpty()) {
           if (categoriaRepository.findByNomeAndCompanyId(nome, companyId).isPresent() &&
                   !categoria.getNome().equals(nome)) {
               throw new RuntimeException("Já existe uma categoria com este nome");
           }
           categoria.setNome(nome);
       }
       if (descricao != null) {
           categoria.setDescricao(descricao);
       }
       return categoriaRepository.save(categoria);
    }

    @Transactional
    public void deletarCategoria(Long categoriaId, Long companyId) {
        CategoriaProduto categoria = buscarCategoriaPorId(categoriaId, companyId);

        // Verificar se existem produtos nessa categoria
        if (!categoria.getProdutos().isEmpty()) {
            throw new RuntimeException("Não é possível deletar categoria com produtos. Mova ou delete os produtos primeiro.");
        }

        categoriaRepository.delete(categoria);
    }

    //Produtos

    @Transactional
    public Produto criarProduto(Company company, Long categoriaId, String nome, String descricao,
                                BigDecimal preco, Integer quantidade) {

        CategoriaProduto categoria = buscarCategoriaPorId(categoriaId, company.getId());

        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setPreco(preco);
        produto.setQuantidade(quantidade);
        produto.setCategoria(categoria);
        produto.setCompany(company);

        return produtoRepository.save(produto);
    }

    public List<Produto> listarProdutos(Long companyId) {
        return produtoRepository.findByCompanyId(companyId);
    }

    public List<Produto> listarProdutosPorCategoria(Long categoriaId, Long companyId) {
        // Verificar se categoria pertence à empresa
        buscarCategoriaPorId(categoriaId, companyId);
        return produtoRepository.findByCategoriaIdAndCompanyId(categoriaId, companyId);
    }

    public Produto buscarProdutoPorId(Long produtoId, Long companyId) {
        return produtoRepository.findByIdAndCompanyId(produtoId, companyId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
    }

    @Transactional
    public Produto atualizarProduto(Long produtoId, Long companyId, String nome, String descricao,
                                    BigDecimal preco, Integer quantidade, Long categoriaId) {

        Produto produto = buscarProdutoPorId(produtoId, companyId);

        if (nome != null && !nome.isEmpty()) {
            produto.setNome(nome);
        }

        if (descricao != null) {
            produto.setDescricao(descricao);
        }

        if (preco != null && preco.compareTo(BigDecimal.ZERO) > 0) {
            produto.setPreco(preco);
        }

        if (quantidade != null && quantidade >= 0) {
            produto.setQuantidade(quantidade);
        }

        if (categoriaId != null) {
            CategoriaProduto categoria = buscarCategoriaPorId(categoriaId, companyId);
            produto.setCategoria(categoria);
        }

        return produtoRepository.save(produto);
    }

    @Transactional
    public void deletarProduto(Long produtoId, Long companyId) {
        Produto produto = buscarProdutoPorId(produtoId, companyId);
        produtoRepository.delete(produto);
    }

    public List<Produto> buscarProdutosPorNome(Long companyId, String search) {
        return produtoRepository.searchByNome(companyId, search);
    }

    public List<Produto> buscarProdutosEstoqueBaixo(Long companyId, Integer minimo) {
        return produtoRepository.findProdutosComEstoqueBaixo(companyId, minimo);
    }
}
