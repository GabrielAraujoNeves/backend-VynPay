package VynPay.Vynpay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MesaResponseDTO {
    private Long id;
    private Integer numeroMesa;
    private Integer capacidade;
    private Boolean isOcupada;
    private LocalDateTime createdAt;
}