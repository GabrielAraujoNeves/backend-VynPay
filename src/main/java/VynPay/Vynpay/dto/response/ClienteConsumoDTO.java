package VynPay.Vynpay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class ClienteConsumoDTO {
    private Long id;
    private String nome;
    private BigDecimal valorTotal;
    private List<ItemConsumoDTO> itens;
}