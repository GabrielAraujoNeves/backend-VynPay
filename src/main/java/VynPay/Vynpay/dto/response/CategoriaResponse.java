package VynPay.Vynpay.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoriaResponse {
    private Long id;
    private String nome;
    private String descricao;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}