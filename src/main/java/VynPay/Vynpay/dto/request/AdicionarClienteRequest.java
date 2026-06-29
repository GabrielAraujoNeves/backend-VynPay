package VynPay.Vynpay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdicionarClienteRequest {
    @NotBlank(message = "O nome do cliente é obrigatório")
    private String nome;
}