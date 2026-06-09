package VynPay.Vynpay.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CartaoEdificacaoRequest {
    @NotBlank
    private String numeroCartao;

    private String nomeCliente;

    private String cartaoVinculado;
}