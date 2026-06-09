package VynPay.Vynpay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PulseiraRequest {
    @NotBlank
    private String numeroPulseira;

    private String nomeCliente;

    private String pulseiraAgrupadaCom;
}