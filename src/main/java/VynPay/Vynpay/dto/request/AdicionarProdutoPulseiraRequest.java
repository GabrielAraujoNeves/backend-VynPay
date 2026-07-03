package VynPay.Vynpay.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AdicionarProdutoPulseiraRequest {

    @NotBlank(message = "Número da pulseira é obrigatório")
    @Pattern(regexp = "^[0-9]+$", message = "Número da pulseira deve conter apenas números")
    private String numeroPulseira;

    @NotNull(message = "ID do produto é obrigatório")
    private Long produtoId;

    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantidade;
}