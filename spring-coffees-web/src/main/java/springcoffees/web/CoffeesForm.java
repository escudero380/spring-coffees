package springcoffees.web;

import lombok.Data;
import springcoffees.domain.Coffee;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
class CoffeesForm {

    /*
     * Marker interfaces for validation group.
     */

    interface QuantityAware {
    }

    interface PriceAware {
    }

    /*
     * Fields and their validation constraints.
     */

    @Min(value = 0, message = "Coffee id must be non-negative integer value")
    private int id;

    @NotBlank(message = "Coffee name must not be empty")
    private String name;

    @NotBlank(message = "Coffee origin must not be empty")
    private String origin;

    @Min(value = 1, message = "Choose from existing suppliers (if there is no one - " +
            "go to 'Suppliers' table and add a new supplier first)")
    private int supplierId;

    @NotNull(groups = {CoffeesForm.PriceAware.class},
            message = "Coffee price must not be empty")
    @DecimalMin(groups = {CoffeesForm.PriceAware.class},
            value = "0.00", inclusive = false, message = "Coffee price must be a non-zero positive value")
    @Digits(groups = {CoffeesForm.PriceAware.class},
            integer = 3, fraction = 2, message = "Coffee price must be a valid decimal value like 17.99")
    private BigDecimal currentPrice;

    private int stock;

    @Min(groups = {CoffeesForm.QuantityAware.class},
            value = 0, message = "Coffee quantity can't be a negative value")
    @Max(groups = {CoffeesForm.QuantityAware.class},
            value = 1000, message = "You can't buy or sell more than 1 000 at once")
    private int quantity;

    private String formAction;

    /*
     * A convenience converter from this
     * form into Coffee domain object.
     */

    Coffee toCoffee() {
        return new Coffee(id, name, origin,
                supplierId, null,
                currentPrice, stock);
    }

}
