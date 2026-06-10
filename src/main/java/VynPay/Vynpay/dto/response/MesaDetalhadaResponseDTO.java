package VynPay.Vynpay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class MesaDetalhadaResponseDTO {
    private Long id;
    private Integer numeroMesa;
    private Integer capacidade;
    private Boolean isOcupada;
    private LocalDateTime createdAt;
    private ComandaInfoDTO comanda; // Informações da comanda da mesa
}