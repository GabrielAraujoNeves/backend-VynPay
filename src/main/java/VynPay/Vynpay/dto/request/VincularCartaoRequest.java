package VynPay.Vynpay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VincularCartaoRequest {
    @NotBlank
    private String cartaoPrincipal;

    @NotBlank
    private String cartaoSecundario;
}