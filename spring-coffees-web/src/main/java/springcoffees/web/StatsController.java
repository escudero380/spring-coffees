package springcoffees.web;

import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springcoffees.domain.Stats;
import springcoffees.jdbc.StatsRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/stats")
@SessionAttributes("statsFilters")
@Validated
public class StatsController {

    private final StatsRepository statsRepo;

    @Autowired
    public StatsController(StatsRepository statsRepo) {
        this.statsRepo = statsRepo;
    }

    @ModelAttribute("statsFilters")
    Map<String, Object> setDefaultFilters() {
        // by default, stats table is set to show top 5
        // best selling coffees for the last two months
        HashMap<String, Object> filters = new HashMap<>();
        filters.put("topSeller", Stats.TopSellers.COFFEES);
        filters.put("months", 2);
        filters.put("limit", 5);
        return filters;
    }

    @GetMapping()
    public String statisticsPageRouter(@ModelAttribute("statsFilters") Map<String, Object> filters) {
        String pathVariable = filters.get("topSeller").toString().toLowerCase();
        return "redirect:/stats/" + pathVariable;
    }

    @GetMapping("/{topSeller}")
    public String showTopSellers(Model model,
                                 @ModelAttribute("statsFilters") Map<String, Object> filters,
                                 @PathVariable String topSeller,
                                 @RequestParam(name = "months", required = false)
                                 @Range(min = 1, max = 12, message = "Parameter 'months' must be from 1 to 12")
                                         Integer monthsParam,
                                 @RequestParam(name = "limit", required = false)
                                 @Range(min = 1, max = 10, message = "Parameter 'limit' must be from 1 to 10")
                                         Integer limitParam) {
        // if path variable doesn't match to enum constant - show 404 page
        Stats.TopSellers seller;
        try {
            seller = Stats.TopSellers.valueOf(topSeller.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return "forward:/404";
        }
        // if no 'months' parameter is given, use
        // the one from filters session attribute
        int months = Objects.requireNonNullElseGet(monthsParam, () -> (Integer) filters.get("months"));
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);
        // if no 'limit' parameter is given, use
        // the one from filters session attribute
        int limit = Objects.requireNonNullElseGet(limitParam, () -> (Integer) filters.get("limit"));
        Stats stats = statsRepo.topSelling(seller, startDate, endDate, limit);
        model.addAttribute("stats", stats);
        // refresh 'filters' session attribute
        filters.put("topSeller", topSeller);
        filters.put("months", months);
        filters.put("limit", limit);
        model.addAttribute("statsFilters", filters);
        return "stats";
    }

}
