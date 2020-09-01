package springcoffees.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@Setter(AccessLevel.NONE)
public class Stats {

    private final List<String> headers;
    private final List<List<String>> results;
    private final long totalQuantity;
    private final BigDecimal totalSum;

    public enum TopSellers {
        COFFEES, SUPPLIERS, MANGERS
    }

}
