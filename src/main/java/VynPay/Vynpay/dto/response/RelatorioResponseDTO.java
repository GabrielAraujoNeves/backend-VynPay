package VynPay.Vynpay.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioResponseDTO(
        LocalDate data,
        BigDecimal totalVendas,
        Long quantidadeComandas,
        List<ResumoComandaDTO> comandas
) {}