package springcoffees.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import springcoffees.domain.Supplier;
import springcoffees.jdbc.SuppliersRepository;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/suppliers")
@SessionAttributes("suppliersOrderByNameFilter")
public class SuppliersController {
    private final SuppliersRepository suppliersRepo;

    @Autowired
    public SuppliersController(SuppliersRepository suppliersRepo) {
        this.suppliersRepo = suppliersRepo;
    }

    @ModelAttribute("suppliersOrderByNameFilter")
    boolean setDefaultFilter() {
        // shows unordered suppliers list by default
        return false;
    }

    @GetMapping("/filters")
    public String updateFilter(@RequestParam(value = "orderByName", required = false, defaultValue = "false")
                                       boolean orderByNameParam, Model model) {
        model.addAttribute("suppliersOrderByNameFilter", orderByNameParam);
        return "redirect:/suppliers";
    }

    @GetMapping
    public String showSuppliers(Model model, @ModelAttribute("suppliersOrderByNameFilter") boolean isOrderByName) {
        List<Supplier> list = (isOrderByName)
                ? suppliersRepo.findAllOrderByName()
                : suppliersRepo.findAll();
        model.addAttribute("suppliersList", list);
        model.addAttribute("suppliersForm", new SuppliersForm());
        return "suppliers";
    }

    @PostMapping("/delete")
    public String deleteSupplier(@ModelAttribute @Valid SuppliersForm form, BindingResult errors) {
        if (!errors.hasErrors()) {
            Supplier supplier = form.toSupplier();
            try {
                suppliersRepo.delete(supplier);
                return "redirect:/suppliers";
            } catch (DataIntegrityViolationException ex) {
                // reject suppliers that can't be deleted because
                // they are referenced by records in other tables
                errors.rejectValue("name", null, ex.getMessage());
            }
        }
        return "suppliers";
    }

    @PostMapping(value = {"/save", "/update"})
    public String addOrUpdateSupplier(@ModelAttribute @Valid SuppliersForm form, BindingResult errors) {
        if (!errors.hasErrors()) {
            Supplier supplier = form.toSupplier();
            try {
                // adds new supplier when supplier id is 0,
                // or updates existing supplier otherwise
                suppliersRepo.save(supplier);
                return "redirect:/suppliers";
            } catch (DataIntegrityViolationException ex) {
                // reject suppliers with duplicate e-mails
                errors.rejectValue("email", null, ex.getMessage());
            }
        }
        return "suppliers";
    }

}

