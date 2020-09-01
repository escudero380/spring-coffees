package springcoffees.domain;

import lombok.*;

@Data
@AllArgsConstructor
public class Supplier {

    private int id;
    private final String name;
    private final String email;
    private final String address;

}