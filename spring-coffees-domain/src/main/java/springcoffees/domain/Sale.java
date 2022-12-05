package springcoffees.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Sale {

    private final long id;
    private final String coffeeTuple;
    private final int supplierId;
    private final String supplierName;
    private final String manger;
    private final LocalDateTime dateTime;
    private final int saleQuantity;
    private final BigDecimal saleSum;

}
