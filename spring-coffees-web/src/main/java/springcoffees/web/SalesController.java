package springcoffees.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.Formatter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import springcoffees.domain.*;
import springcoffees.jdbc.*;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/sales")
@ConfigurationProperties(prefix = "springcoffees.web")
@SessionAttributes({"salesFilters", "salesDateTimeDirection", "salesPageNum"})
@Validated
public class SalesController {
    private final SalesRepository salesRepository;
    private final CoffeesRepository coffeesRepository;
    private final SuppliersRepository suppliersRepository;
    private final UsersRepository usersRepository;

    // this value is set from the application properties file
    private int pageSize;

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @ModelAttribute("salesFilters")
    Map<String, Object> setDefaultFilters() {
        // by default, sales are not filtered out
        return new HashMap<>();
    }

    @ModelAttribute("salesDateTimeDirection")
    Sort.Direction setDefaultDateTimeDirection() {
        // by default, sales are in ASC date-time order
        return Sort.Direction.ASC;
    }

    @ModelAttribute("salesPageNum")
    int setDefaultPageNum() {
        // by default, the first page of sales is shown
        return 1;
    }

    @Autowired
    public SalesController(SalesRepository salesRepository, CoffeesRepository coffeesRepository,
                           SuppliersRepository suppliersRepository, UsersRepository usersRepository) {
        this.salesRepository = salesRepository;
        this.coffeesRepository = coffeesRepository;
        this.suppliersRepository = suppliersRepository;
        this.usersRepository = usersRepository;
    }

    @GetMapping("/filters")
    public String updateFilters(@ModelAttribute("salesFilters") Map<String, Object> filters,
                                @RequestParam("coffeeTuple") Optional<String> coffeeTuple,
                                @RequestParam("supplierId") Optional<Integer> supplierId,
                                @RequestParam("saleManager") Optional<String> saleManager,
                                @DateTimeFormat(pattern = "dd.MM.yyyy")
                                @RequestParam("startDate") Optional<LocalDate> startDate,
                                @DateTimeFormat(pattern = "dd.MM.yyyy")
                                @RequestParam("endDate") Optional<LocalDate> endDate) {
        filters.put("coffee_tuple", coffeeTuple.filter(tuple -> !tuple.isBlank()).orElse(null));
        filters.put("supplier_id", supplierId.filter(id -> id > 0).orElse(null));
        filters.put("sale_manager", saleManager.filter(manager -> !manager.isBlank()).orElse(null));
        filters.put("start_date", startDate.map(LocalDate::atStartOfDay).orElse(null));
        filters.put("end_date", endDate.map(date -> date.atTime(LocalTime.MAX)).orElse(null));
        filters.values().removeIf(Objects::isNull);
        return "redirect:/sales";
    }

    @GetMapping
    public String showSales(Model model,
                            @ModelAttribute("salesFilters") Map<String, Object> filters,
                            @ModelAttribute("salesPageNum") int currentPageNum,
                            @ModelAttribute("salesDateTimeDirection") Sort.Direction currentDirection,
                            @Min(value = 1, message = "Page index must not be less than 1")
                            @RequestParam(value = "page", required = false) Integer pageNumParam,
                            @RequestParam(value = "sort", required = false) Sort sortParam) {
        // if page and sort parameters are present in request, use them and update
        // relevant session attributes, or use current attributes values otherwise
        int newPageNum;
        try {
            newPageNum = Objects.requireNonNull(pageNumParam);
            model.addAttribute("salesPageNum", newPageNum);
        } catch (NullPointerException ex) {
            newPageNum = currentPageNum;
        }
        Sort newDateTimeSort;
        try {
            Sort.Order newOrder = Objects.requireNonNull(sortParam.getOrderFor("dateTime"));
            model.addAttribute("salesDateTimeDirection", newOrder.getDirection());
            newDateTimeSort = sortParam;
        } catch (NullPointerException ex) {
            newDateTimeSort = Sort.by(currentDirection, "dateTime");
        }

        // check whether session filtering parameters still exist in the database
        // (in case they may have been deleted from the database by other users)
        List<String> coffeeTuplesList = coffeesRepository.findCoffeeTuplesDistinct();
        String sessionCoffeeTuple = (String) filters.get("coffee_tuple");
        if (sessionCoffeeTuple != null && !coffeeTuplesList.contains(sessionCoffeeTuple)) {
            filters.remove("coffee_tuple");
        }
        List<User> usersList = usersRepository.findAllOrderBy(User.OrderBy.USERNAME);
        String sessionSaleManager = (String) filters.get("sale_manager");
        if (sessionSaleManager != null &&
                usersList.stream().map(User::getUsername).noneMatch(u -> u.equals(sessionSaleManager))) {
            filters.remove("sale_manager");
        }
        List<Supplier> suppliersList = suppliersRepository.findAllOrderByName();
        Integer sessionSupplierId = (Integer) filters.get("supplier_id");
        if (sessionSupplierId != null &&
                suppliersList.stream().mapToInt(Supplier::getId).noneMatch(id -> id == sessionSupplierId)) {
            filters.remove("supplier_id");
        }

        // fetch pageable sales data from repository
        Pageable pageable = PageRequest.of(newPageNum - 1, pageSize, newDateTimeSort);
        MapSqlParameterSource sqlParams = new MapSqlParameterSource(filters);
        SalesPage salesPage = salesRepository.findAllByOrderByDateTime(sqlParams, pageable);

        // set model and view
        SalesForm salesForm = new SalesForm();
        model.addAttribute("salesForm", salesForm);
        model.addAttribute("salesPage", salesPage);
        model.addAttribute("coffeeTuplesList", coffeeTuplesList);
        model.addAttribute("suppliersList", suppliersList);
        model.addAttribute("usersList", usersList);
        return "sales";
    }

    @PostMapping("/delete")
    public String deleteSales(@ModelAttribute SalesForm salesForm, BindingResult errors) {
        if (errors.hasErrors())
            throw new IllegalArgumentException("Deletion operation rejected because of " +
                    "inappropriate arguments have been passed.");
        int[] ids = salesForm.getSelectedSales();
        if (ids != null && ids.length > 0)
            salesRepository.deleteSelected(ids);
        return "redirect:/sales";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgumentErrors(IllegalArgumentException ex) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", "Wrong arguments");
        mav.addObject("message", "This request cannot be performed " +
                "due to unintentional or malicious code violation:");
        String err = Objects.requireNonNullElse(ex.getMessage(), "Unresolved");
        List<String> errors = List.of(err);
        mav.addObject("errors", errors);
        mav.setViewName("error/500");
        return mav;
    }

    /*
     * Custom formatter to parse a String value for the 'sort' request
     * parameter like "property,asc" into corresponding Sort object (if
     * direction part is omitted, then it's set to be of an ASC order)
     */

    @InitBinder
    public void customizeBinding(WebDataBinder binder) {
        binder.addCustomFormatter(new Formatter<Sort>() {
            @Override
            public Sort parse(String s, Locale locale) {
                String[] sortParts = s.split(",");
                String property = sortParts[0].trim();
                Sort.Direction direction = (sortParts.length > 1)
                        ? Sort.Direction.fromString(sortParts[1].trim())
                        : Sort.Direction.ASC;
                return Sort.by(direction, property);
            }

            @Override
            public String print(Sort orders, Locale locale) {
                return orders.toString();
            }
        });
    }

//    // as an alternative, we could declare context-aware
//    // String to Sort converter with very same semantics
//    @Component
//    public static class StringToSortConverter implements Converter<String, Sort> {
//        @Override
//        public Sort convert(String value) {
//            // converts String value like "property,asc" or "property,desc" into Sort object
//            // (if direction part is omitted, then it's assumed to be of an ascending order)
//            String[] sortParts = value.split(",");
//            String property = sortParts[0].trim();
//            Sort.Direction direction = (sortParts.length > 1)
//                    ? Sort.Direction.fromString(sortParts[1].trim())
//                    : Sort.Direction.ASC;
//            return Sort.by(direction, property);
//        }
//    }

}

