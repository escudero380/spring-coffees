package springcoffees.jdbc;

import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import springcoffees.domain.SalesPage;

public interface SalesRepository {

    SalesPage findAllByOrderByDateTime(MapSqlParameterSource namedParameters, Pageable pageable);

    void deleteSelected(int[] saleIds);

}
