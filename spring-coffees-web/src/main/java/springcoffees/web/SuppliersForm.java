package springcoffees.web;

import lombok.Data;
import springcoffees.domain.Supplier;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
class SuppliersForm {

    @Min(value = 0, message = "Supplier id must be a non-negative integer value")
    int id;

    @NotBlank(message = "Supplier name must not be empty")
    private String name;

    @NotBlank(message = "Supplier e-mail must not be empty")
    @Email(message = "Please, provide valid supplier e-mail")
    private String email;

    @NotBlank(message = "Supplier address must not be empty")
    private String address;

    private String formAction;

    // a helper method to convert suppliers form into supplier object
    Supplier toSupplier() {
        return new Supplier(id, name, email, address);
    }

}
