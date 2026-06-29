package VynPay.Vynpay.service;

import VynPay.Vynpay.dto.response.*;
import VynPay.Vynpay.enun.StatusComanda;
import VynPay.Vynpay.model.*;
import VynPay.Vynpay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    @Autowired
    private ComandaRepository comandaRepository;

    @Autowired
    private ComandaItemRepository comandaItemRepository;

    @Autowired
    private ClienteComandaRepository clienteComandaRepository;

    @Autowired
    private LogRemocaoItemRepository logRemocaoItemRepository;

    // ============================================
    // VENDAS POR DIA, MÊS E ANO
    // ============================================

    @Transactional(readOnly = true)
    public RelatorioVendasResponseDTO getVendasPorPeriodo(LocalDate dataInicio, LocalDate dataFim, Long companyId) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(LocalTime.MAX);

        // 1. Buscar comandas pagas no período
        List<Comanda> comandasPagas = comandaRepository.findComandasPagasPorPeriodo(inicio, fim, companyId);

        // 2. Total de vendas
        BigDecimal totalVendas = comandasPagas.stream()
                .map(Comanda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Total de comandas
        Long totalComandas = (long) comandasPagas.size();

        // 4. Total de clientes únicos
        List<ClienteComanda> clientes = clienteComandaRepository.findByComandaIn(comandasPagas);
        Long totalClientes = clientes.stream()
                .map(ClienteComanda::getNome)
                .distinct()
                .count();

        // 5. Vendas por dia
        List<ResumoVendasDTO> vendasPorDia = new ArrayList<>();
        LocalDate dataAtual = dataInicio;

        while (!dataAtual.isAfter(dataFim)) {
            LocalDate finalDataAtual = dataAtual;
            List<Comanda> comandasDia = comandasPagas.stream()
                    .filter(c -> c.getDataFechamento() != null &&
                            c.getDataFechamento().toLocalDate().equals(finalDataAtual))
                    .collect(Collectors.toList());

            BigDecimal totalDia = comandasDia.stream()
                    .map(Comanda::getValorTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<Long> comandaIds = comandasDia.stream()
                    .map(Comanda::getId)
                    .collect(Collectors.toList());

            List<ResumoProdutoDTO> produtosMaisVendidos = new ArrayList<>();

            if (!comandaIds.isEmpty()) {
                try {
                    List<Object[]> resultados = comandaItemRepository.findProdutosMaisVendidos(comandaIds);
                    produtosMaisVendidos = resultados.stream()
                            .map(r -> ResumoProdutoDTO.builder()
                                    .produtoId((Long) r[0])
                                    .nomeProduto((String) r[1])
                                    .quantidadeVendida(((Number) r[2]).intValue())
                                    .valorTotal((BigDecimal) r[3])
                                    .build())
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    System.err.println("Erro ao buscar produtos mais vendidos: " + e.getMessage());
                }
            }

            ResumoVendasDTO resumoDia = ResumoVendasDTO.builder()
                    .data(dataAtual)
                    .totalDia(totalDia)
                    .quantidadeVendas((long) comandasDia.size())
                    .produtosMaisVendidos(produtosMaisVendidos)
                    .build();

            vendasPorDia.add(resumoDia);
            dataAtual = dataAtual.plusDays(1);
        }

        return RelatorioVendasResponseDTO.builder()
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .totalVendas(totalVendas)
                .totalComandas(totalComandas)
                .totalClientes(totalClientes)
                .vendasPorDia(vendasPorDia)
                .build();
    }

    // ============================================
    // ITENS PERDIDOS / CANCELADOS
    // ============================================

    @Transactional(readOnly = true)
    public RelatorioPerdidosDTO getItensPerdidos(LocalDate dataInicio, LocalDate dataFim, Long companyId) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(LocalTime.MAX);

        // Buscar logs de remoção
        List<LogRemocaoItem> logs = logRemocaoItemRepository.findByDataRemocaoBetweenAndCompanyId(inicio, fim, companyId);

        if (logs.isEmpty()) {
            return RelatorioPerdidosDTO.builder()
                    .totalItensPerdidos(0L)
                    .valorTotalPerdido(BigDecimal.ZERO)
                    .itensCancelados(new ArrayList<>())
                    .build();
        }

        // Calcular totais
        Long totalItens = logs.stream()
                .mapToLong(LogRemocaoItem::getQuantidade)
                .sum();

        BigDecimal valorTotal = logs.stream()
                .map(LogRemocaoItem::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Converter para DTO
        List<ItemCanceladoDTO> itensCancelados = logs.stream()
                .map(log -> ItemCanceladoDTO.builder()
                        .itemId(log.getItemId())
                        .produtoNome(log.getProdutoNome())
                        .quantidade(log.getQuantidade())
                        .precoUnitario(log.getPrecoUnitario())
                        .valorTotal(log.getValorTotal())
                        .clienteNome(log.getClienteNome())
                        .justificativa(log.getJustificativa())
                        .removidoPor(log.getRemovidoPor())
                        .dataRemocao(log.getDataRemocao())
                        .build())
                .collect(Collectors.toList());

        return RelatorioPerdidosDTO.builder()
                .totalItensPerdidos(totalItens)
                .valorTotalPerdido(valorTotal)
                .itensCancelados(itensCancelados)
                .build();
    }

    // ============================================
    // RESUMO GERAL (Dashboard)
    // ============================================

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(Long companyId) {
        Map<String, Object> dashboard = new HashMap<>();

        LocalDate hoje = LocalDate.now();
        LocalDate primeiroDiaMes = hoje.withDayOfMonth(1);
        LocalDate primeiroDiaAno = hoje.withDayOfYear(1);

        // Vendas hoje
        List<Comanda> comandasHoje = comandaRepository.findComandasPagasPorDia(hoje, companyId);
        BigDecimal totalHoje = comandasHoje.stream()
                .map(Comanda::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Vendas mês
        LocalDateTime inicioMes = primeiroDiaMes.atStartOfDay();
        LocalDateTime fimHoje = hoje.atTime(LocalTime.MAX);
        BigDecimal totalMes = comandaRepository.sumVendasPorPeriodo(inicioMes, fimHoje, companyId);

        // Vendas ano
        LocalDateTime inicioAno = primeiroDiaAno.atStartOfDay();
        BigDecimal totalAno = comandaRepository.sumVendasPorPeriodo(inicioAno, fimHoje, companyId);

        // Itens perdidos hoje
        List<LogRemocaoItem> logsHoje = logRemocaoItemRepository.findByDataRemocaoBetweenAndCompanyId(
                hoje.atStartOfDay(), hoje.atTime(LocalTime.MAX), companyId
        );
        Long itensPerdidosHoje = logsHoje.stream()
                .mapToLong(LogRemocaoItem::getQuantidade)
                .sum();

        // Comandas abertas
        Long comandasAbertas = (long) comandaRepository.findByCompanyIdAndStatus(companyId, StatusComanda.ABERTA).size();

        dashboard.put("vendasHoje", totalHoje);
        dashboard.put("vendasMes", totalMes);
        dashboard.put("vendasAno", totalAno);
        dashboard.put("itensPerdidosHoje", itensPerdidosHoje);
        dashboard.put("comandasAbertas", comandasAbertas);
        dashboard.put("dataAtual", hoje);

        return dashboard;
    }
}