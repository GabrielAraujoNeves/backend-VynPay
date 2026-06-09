package VynPay.Vynpay.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MesaRequest {
    @NotNull
    private Integer numeroMesa;

    @NotNull
    private Integer capacidade;
}