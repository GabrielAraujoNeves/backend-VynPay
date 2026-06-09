package VynPay.Vynpay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgruparPulseiraRequest {
    @NotBlank
    private String pulseiraPrincipal;

    @NotBlank
    private String pulseiraSecundaria;
}