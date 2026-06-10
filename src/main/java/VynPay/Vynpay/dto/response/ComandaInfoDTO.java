package VynPay.Vynpay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ComandaInfoDTO {
    private Long comandaId;
    private String numeroComanda;
    private LocalDateTime dataAbertura;
    private BigDecimal valorTotal;
    private List<ClienteConsumoDTO> clientes;
}