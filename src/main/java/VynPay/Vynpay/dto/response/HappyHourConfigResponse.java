package VynPay.Vynpay.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
public class HappyHourConfigResponse {
    private Long id;
    private Boolean isActive;
    private Double discountPercent;
    private LocalTime startTime;
    private LocalTime endTime;
    private List<HappyHourProductResponse> products;
}