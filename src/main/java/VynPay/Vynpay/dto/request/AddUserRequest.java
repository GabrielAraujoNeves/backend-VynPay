package VynPay.Vynpay.dto.request;

import lombok.Data;

@Data
public class AddUserRequest {
    private String login;
    private String password;
}