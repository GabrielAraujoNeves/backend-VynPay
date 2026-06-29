package VynPay.Vynpay.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RelatorioPerdidosDTO {
    private Long totalItensPerdidos;
    private BigDecimal valorTotalPerdido;
    private List<ItemCanceladoDTO> itensCancelados;
}

