package VynPay.Vynpay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class HappyHourProductResponse {
    private Long productId;
    private String productName;
    private BigDecimal originalPrice;
    private BigDecimal discountedPrice;
}