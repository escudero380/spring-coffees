package springcoffees.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Coffee {

    private int id;
    private final String name;
    private final String origin;
    private final int supplierId;
    private final String supplierName;
    private final BigDecimal currentPrice;
    private final int stock;

    public enum OrderBy {

        NONE("id"),
        NAME("name"),
        PRICE("current_price"),
        STOCK("stock");

        private final String dbField;

        OrderBy(String dbField) {
            this.dbField = dbField;
        }

        public String getDbField() {
            return dbField;
        }

    }

}
