package VynPay.Vynpay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenValidationResponse {
    private Boolean valid;
    private String message;
    private Long userId;
    private String email;
    private String username;
    private String role;
    private Long companyId;
    private String companyName;
}