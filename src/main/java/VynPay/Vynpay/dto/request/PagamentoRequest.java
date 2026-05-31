package VynPay.Vynpay.dto.request;


import VynPay.Vynpay.enun.FormaPagamento;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PagamentoRequest {
    private BigDecimal valorPago;
    private FormaPagamento formaPagamento;
}