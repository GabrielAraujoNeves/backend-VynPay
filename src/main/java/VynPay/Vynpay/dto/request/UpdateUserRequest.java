package VynPay.Vynpay.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String login;    // opcional
    private String password; // opcional
    private String role;     // opcional (USER ou ADMIN)
}