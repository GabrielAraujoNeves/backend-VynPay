package VynPay.Vynpay.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ResumoComandaDTO(
        Long id,
        String numeroComanda,
        LocalDateTime dataAbertura,
        String status,
        BigDecimal valorTotal,
        String tipoComanda,
        String identificador,
        List<ResumoItemDTO> itens,
        List<ResumoClienteDTO> clientes
) {}