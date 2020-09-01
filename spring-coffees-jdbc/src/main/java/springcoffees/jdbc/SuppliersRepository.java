package springcoffees.jdbc;

import springcoffees.domain.Supplier;

import java.util.List;

public interface SuppliersRepository {

    List<Supplier> findAll();

    List<Supplier> findAllOrderByName();

    /* Adds new supplier when supplier id is 0,
        or updates existing supplier otherwise */
    Supplier save(Supplier supplier);

    void delete(Supplier supplier);

}

