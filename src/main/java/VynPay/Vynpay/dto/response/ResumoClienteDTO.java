package VynPay.Vynpay.dto.response;

import java.math.BigDecimal;

public record ResumoClienteDTO(
        Long id,
        String nome,
        BigDecimal valorTotal
) {}