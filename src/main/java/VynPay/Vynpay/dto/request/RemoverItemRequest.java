package VynPay.Vynpay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemoverItemRequest {

    @NotNull(message = "O ID do item e obrigatorio")
    private Long itemId;

    @NotBlank(message = "A justificativa e obrigatoria")
    private String justificativa;
}
