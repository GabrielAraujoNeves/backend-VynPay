package VynPay.Vynpay.dto.request;

import VynPay.Vynpay.model.Company;
import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private Company company;
}
