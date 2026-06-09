package VynPay.Vynpay.dto.response;

import java.math.BigDecimal;

public record ResumoItemDTO(
        Long id,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal precoTotal,
        String produtoNome,
        BigDecimal produtoPreco
) {}