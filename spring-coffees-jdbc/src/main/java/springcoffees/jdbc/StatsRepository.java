package springcoffees.jdbc;

import springcoffees.domain.Stats;

import java.time.LocalDate;

public interface StatsRepository {

    Stats topSelling(Stats.TopSellers sellers, LocalDate startDate, LocalDate endDate, int limit);

    Stats topSellingCoffees(LocalDate startDate, LocalDate endDate, int limit);

    Stats topSellingSuppliers(LocalDate startDate, LocalDate endDate, int limit);

    Stats topSellingManagers(LocalDate startDate, LocalDate endDate, int limit);

}
