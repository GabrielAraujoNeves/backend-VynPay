package VynPay.Vynpay.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagarPulseiraRequest {

    @NotBlank(message = "Número da pulseira é obrigatório")
    @Pattern(regexp = "^[0-9]+$", message = "Número da pulseira deve conter apenas números")
    private String numeroPulseira;

    @NotNull(message = "Valor a pagar é obrigatório")
    @Min(value = 0, message = "Valor deve ser maior ou igual a zero")
    private BigDecimal valorPago;

    @NotBlank(message = "Forma de pagamento é obrigatória")
    private String formaPagamento; // DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO, PIX
}