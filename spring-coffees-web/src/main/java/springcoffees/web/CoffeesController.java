package springcoffees.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import springcoffees.domain.Coffee;
import springcoffees.domain.Supplier;
import springcoffees.jdbc.CoffeesRepository;
import springcoffees.jdbc.SuppliersRepository;

import javax.servlet.http.HttpServletRequest;
import javax.validation.groups.Default;
import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/coffees")
@SessionAttributes({"coffeesFilters", "suppliersListInCoffees"})
public class CoffeesController {
    private final SuppliersRepository suppliersRepo;
    private final CoffeesRepository coffeesRepo;

    @Autowired
    public CoffeesController(SuppliersRepository suppliersRepo, CoffeesRepository coffeesRepo) {
        this.suppliersRepo = suppliersRepo;
        this.coffeesRepo = coffeesRepo;
    }

    @ModelAttribute("coffeesFilters")
    Map<String, Object> setDefaultFilters() {
        // by default, coffees should be ordered by their id
        // and filtered out by no supplier (supplier id = 0)
        Map<String, Object> map = new HashMap<>();
        map.put("orderBy", Coffee.OrderBy.NONE);
        map.put("supplierId", 0);
        return map;
    }

    @ModelAttribute("suppliersListInCoffees")
    List<Supplier> fetchSuppliersListForSession() {
        return suppliersRepo.findAllOrderByName();
    }

    @GetMapping("/filters")
    String updateFilters(@ModelAttribute("coffeesFilters") Map<String, Object> filters,
                         @RequestParam("orderBy") Optional<Coffee.OrderBy> orderByParam,
                         @RequestParam("supplierId") Optional<Integer> supplierIdParam) {
        filters.put("orderBy", orderByParam.orElse(Coffee.OrderBy.NONE));
        filters.put("supplierId", supplierIdParam.orElse(0));
        return "redirect:/coffees";
    }

    @GetMapping
    public String showAllCoffees(Model model, @ModelAttribute("coffeesFilters") Map<String, Object> filters) {
        Coffee.OrderBy orderBy = (Coffee.OrderBy) filters.get("orderBy");
        int supplierId = (Integer) filters.get("supplierId");
        List<Supplier> suppliersList = suppliersRepo.findAllOrderByName();
        // check whether supplier from session filters still exists in actual suppliers list,
        // otherwise it may have been deleted from the database but is still kept in session
        boolean stillExists = suppliersList.stream().mapToInt(Supplier::getId).anyMatch(id -> id == supplierId);
        if (stillExists) {
            List<Coffee> coffeesList = coffeesRepo.findBySupplierIdOrderBy(supplierId, orderBy);
            model.addAttribute("coffeesList", coffeesList);
        } else {
            filters.put("supplierId", 0);
            List<Coffee> coffeesList = coffeesRepo.findBySupplierIdOrderBy(0, orderBy);
            model.addAttribute("coffeesList", coffeesList);
        }
        model.addAttribute("suppliersListInCoffees", suppliersList);
        model.addAttribute("coffeesForm", new CoffeesForm());
        return "coffees";
    }

    @PostMapping("/delete")
    public String deleteCoffee(@Validated({Default.class})
                               @ModelAttribute("coffeesForm") CoffeesForm form,
                               BindingResult errors) {
        if (!errors.hasErrors()) {
            Coffee coffee = form.toCoffee();
            try {
                coffeesRepo.delete(coffee);
                return "redirect:/coffees";
            } catch (DataIntegrityViolationException ex) {
                // reject coffees that can't be deleted because
                // they are referenced by records in other tables
                errors.rejectValue("name", null, ex.getMessage());
            }
        }
        return "coffees";
    }

    @PostMapping(path = {"/save", "/update"})
    public String saveOrUpdateCoffee(@Validated({Default.class, CoffeesForm.PriceAware.class})
                                     @ModelAttribute("coffeesForm") CoffeesForm form,
                                     BindingResult errors) {
        if (!errors.hasErrors()) {
            Coffee coffee = form.toCoffee();
            try {
                // adds new coffee when coffee id is 0,
                // or updates existing coffee otherwise
                coffeesRepo.save(coffee);
                return "redirect:/coffees";
            } catch (DataIntegrityViolationException ex) {
                // reject coffees whose supplier no longer exists
                errors.rejectValue("supplierId", null, ex.getMessage());
            }
        }
        return "coffees";
    }

    @PostMapping("/buy")
    public String buyCoffeeQuantity(@Validated({Default.class, CoffeesForm.QuantityAware.class})
                                    @ModelAttribute("coffeesForm") CoffeesForm form,
                                    BindingResult errors) {
        if (!errors.hasErrors()) {
            Coffee coffee = form.toCoffee();
            try {
                coffeesRepo.buy(coffee, form.getQuantity());
                return "redirect:/coffees";
            } catch (DataIntegrityViolationException ex) {
                // reject operation when there is already too much coffee in stock,
                // or when coffee item was earlier removed form the 'Coffees' table
                errors.rejectValue("quantity", null, ex.getMessage());

            }
        }
        return "coffees";
    }

    @PostMapping("/sell")
    public String sellCoffeeQuantity(@Validated({Default.class, CoffeesForm.QuantityAware.class})
                                     @ModelAttribute("coffeesForm") CoffeesForm form,
                                     BindingResult errors, Principal principal) {
        if (!errors.hasErrors()) {
            Coffee coffee = form.toCoffee();
            try {
                boolean isSellingSuccessful = coffeesRepo.sellOrElseAccept(coffee, form.getQuantity(),
                        principal.getName(), (stockBeforeSelling) -> {
                            form.setStock(stockBeforeSelling);
                            errors.rejectValue("quantity", null, "You are trying " +
                                    "to sell more quantity then there is in stock now.");
                        });
                return (isSellingSuccessful) ? "redirect:/coffees" : "coffees";
            } catch (DataIntegrityViolationException ex) {
                // reject operation when the requested coffee item no longer exists
                form.setStock(0);
                errors.rejectValue("quantity", null, ex.getMessage());
            }
        }
        return "coffees";
    }


    /*
     * Custom editors for the 'quantity' and 'currentPrice' fields that prevent result binder
     * from adding TypeMismatch filed error if those fields values can't be properly parsed.
     * Note: we use custom editors only for specific requests where the aforementioned fields
     * don't need to be validated anyway and therefore can be silently ignored.
     */

    @InitBinder("coffeesForm")
    public void customizeBindingResult(WebDataBinder binder, HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();

        if (!requestURL.contains("/buy") && !requestURL.contains("/sell")) {
            binder.registerCustomEditor(Integer.class, "quantity", newCustomQuantityFieldEditor());
        }

        if (!requestURL.contains("/save") && !requestURL.contains("/update")) {
            binder.registerCustomEditor(BigDecimal.class, "currentPrice", newCustomPriceFieldEditor());
        }
    }

    private PropertyEditorSupport newCustomQuantityFieldEditor() {
        return new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                try {
                    setValue(Integer.parseInt(text));
                } catch (NumberFormatException ex) {
                    // here we set 'un-parsable' value from the 'quantity' field to 0,
                    // so that to avoid binding unnecessary TypeMismatch field error
                    setValue(0);
                }
            }
        };
    }

    private PropertyEditorSupport newCustomPriceFieldEditor() {
        return new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                try {
                    BigDecimal value = new BigDecimal(text);
                    setValue(value);
                } catch (NumberFormatException ex) {
                    // set 'un-parsable' value from the 'currentPrice' field to ZERO,
                    // so that to avoid binding unnecessary TypeMismatch field error
                    setValue(BigDecimal.ZERO);
                }
            }
        };
    }

}

