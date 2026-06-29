package VynPay.Vynpay.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class RelatorioVendasResponseDTO {
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private BigDecimal totalVendas;
    private Long totalComandas;
    private Long totalClientes;
    private List<ResumoVendasDTO> vendasPorDia;
}

