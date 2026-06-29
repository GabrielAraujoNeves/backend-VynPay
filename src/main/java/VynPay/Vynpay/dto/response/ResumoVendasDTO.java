package VynPay.Vynpay.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ResumoVendasDTO {
    private LocalDate data;
    private BigDecimal totalDia;
    private Long quantidadeVendas;
    private List<ResumoProdutoDTO> produtosMaisVendidos;
}
