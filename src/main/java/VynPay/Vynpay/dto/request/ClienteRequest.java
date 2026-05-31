package VynPay.Vynpay.dto.request;

import lombok.Data;

@Data
public class ClienteRequest {
    private String nome;
    private String cpf;
    private String telefone;
}