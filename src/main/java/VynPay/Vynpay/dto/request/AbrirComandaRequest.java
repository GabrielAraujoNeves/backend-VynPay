package VynPay.Vynpay.dto.request;

import VynPay.Vynpay.enun.TipoComanda;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class AbrirComandaRequest {
    @NotNull
    private TipoComanda tipoComanda;

    @NotBlank
    private String identificador; // número da mesa, pulseira ou cartão

    private List<String> nomesClientes; // para mesa (múltiplos clientes)

    private String nomeCliente; // para pulseira ou cartão
}