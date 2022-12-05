package springcoffees.domain;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/*
 * Encapsulates information about total sales sum & quantity
 * along with list of sales as per individual page requested.
 */

public class SalesPage {
    private final PageImpl<Sale> page;
    private final BigDecimal totalSum;  // as per database (not per page)
    private final long totalQuantity;   // as per database (not per page)

    public static Builder builder() {
        return new Builder();
    }

    private SalesPage(Builder builder) {
        this.page = new PageImpl<>(builder.salesList, builder.pageable, builder.totalCount);
        this.totalSum = builder.totalSum;
        this.totalQuantity = builder.totalQuantity;
    }

    public PageImpl<Sale> getPage() {
        return page;
    }

    public BigDecimal getTotalSum() {
        return totalSum;
    }

    public long getTotalQuantity() {
        return totalQuantity;
    }

    // sales page builder
    ////////////////////////////////////////////////////////////////
    //--------------------------------------------------------------
    public static class Builder {
        private List<Sale> salesList;
        private Pageable pageable;
        private int totalCount;
        private BigDecimal totalSum;
        private long totalQuantity;

        // sales page builder
        public Builder salesList(List<Sale> salesList) {
            this.salesList = salesList;
            return this;
        }

        public Builder pageable(Pageable pageable) {
            this.pageable = pageable;
            return this;
        }

        public Builder totalCount(int totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public Builder totalSum(BigDecimal totalSum) {
            if (totalSum != null)
                // this is just a protection against malicious BigDecimal impostors
                this.totalSum = (totalSum.getClass() == BigDecimal.class) ? totalSum
                        : new BigDecimal(totalSum.toString());
            return this;
        }

        public Builder totalQuantity(long totalQuantity) {
            this.totalQuantity = totalQuantity;
            return this;
        }

        public SalesPage build() {
            return new SalesPage(this);
        }

        public int getTotalCount() {
            return totalCount;
        }

    }
    //--------------------------------------------------------------
    ////////////////////////////////////////////////////////////////

}
