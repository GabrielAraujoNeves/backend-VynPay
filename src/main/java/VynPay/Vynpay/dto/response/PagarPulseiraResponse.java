package VynPay.Vynpay.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PagarPulseiraResponse {
    private String message;
    private String numeroPulseira;
    private String nomeCliente;
    private BigDecimal valorPago;
    private String formaPagamento;
    private String status;
    private boolean agrupado;
    private Integer totalPulseirasPagas;
}