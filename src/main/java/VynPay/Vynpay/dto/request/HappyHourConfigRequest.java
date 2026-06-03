package VynPay.Vynpay.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalTime;
import java.util.List;

@Data
public class HappyHourConfigRequest {
    @NotNull(message = "Desconto é obrigatório")
    @Min(0) @Max(100)
    private Double discountPercent;

    private LocalTime startTime;

    private LocalTime endTime;

    // 🔥 NOVO: Dias da semana (ex: ["MON", "TUE", "WED", "THU", "FRI"])
    private List<String> daysOfWeek;

    @NotEmpty(message = "Selecione pelo menos um produto")
    private List<Long> productIds;
}