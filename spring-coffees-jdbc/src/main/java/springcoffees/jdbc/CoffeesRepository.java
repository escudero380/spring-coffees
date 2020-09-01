package springcoffees.jdbc;

import springcoffees.domain.Coffee;

import java.util.List;
import java.util.function.IntConsumer;

public interface CoffeesRepository {

    List<Coffee> findBySupplierIdOrderBy(int SupplierId, Coffee.OrderBy orderBy);

    /* Adds new coffee when coffee id is 0,
        or updates existing coffee otherwise */
    Coffee save(Coffee coffee);

    void delete(Coffee coffee);

    /* Returns true if selling was a success, or false - otherwise. As an argument
        of IntConsumer, provides actual stock available just before selling starts  */
    boolean sellOrElseAccept(Coffee coffee, int quantity, String manager,
                             IntConsumer stockBeforeSellingAcceptor);

    void buy(Coffee coffee, int quantity);

    List<String> findCoffeeTuplesDistinct();

}
