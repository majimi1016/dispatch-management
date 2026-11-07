package com.example.dispatch;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/dispatches")
public class DispatchController {

    private final DispatchRepository repository;
    private final CompanyRepository companyRepository;
    private final ItemRepository itemRepository;

    public DispatchController(DispatchRepository repository,
                              CompanyRepository companyRepository,
                              ItemRepository itemRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.itemRepository = itemRepository;
    }

    // 画面で使う選択肢
    @ModelAttribute("companies")
    public List<Company> companies() {
        return companyRepository.findAllByOrderByNameAsc();
    }

    @ModelAttribute("items")
    public List<Item> items() {
        return itemRepository.findAllByOrderByNameAsc();
    }

    @ModelAttribute("vehicleSizes")
    public VehicleSize[] vehicleSizes() {
        return VehicleSize.values();
    }

    @ModelAttribute("shifts")
    public Shift[] shifts() {
        return Shift.values();
    }

    // 配達一覧
    @GetMapping
    public String list(Model model) {
        model.addAttribute("dispatches", repository.findAllByOrderByDispatchDateAsc());
        return "dispatches/list";
    }

    // 配達入力画面
    @GetMapping("/new")
    public String newForm(Model model) {
        DispatchForm form = new DispatchForm();
        form.setShift(Shift.DAY);              // 最初は「昼」
        form.getLines().add(new DispatchLine()); // 車格の行を1行用意しておく
        model.addAttribute("dispatchForm", form);
        return "dispatches/form";
    }

    // 登録処理
    @PostMapping
    public String create(@Valid @ModelAttribute DispatchForm dispatchForm,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "dispatches/form";
        }

        // 車格の行ごとに、配達を1件ずつ作る
        List<Dispatch> dispatches = new ArrayList<>();
        for (DispatchLine line : dispatchForm.getLines()) {
            Dispatch d = new Dispatch();
            d.setDispatchDate(dispatchForm.getDispatchDate());
            d.setShift(dispatchForm.getShift());
            d.setCompany(dispatchForm.getCompany());
            d.setItem(dispatchForm.getItem());
            d.setDestination(dispatchForm.getDestination());
            d.setRemarks(dispatchForm.getRemarks());
            d.setVehicleSize(line.getVehicleSize());
            d.setQuantity(line.getQuantity());
            dispatches.add(d);
        }

        repository.saveAll(dispatches);
        redirectAttributes.addFlashAttribute("message",
                dispatches.size() + "件の配達を登録しました");
        return "redirect:/dispatches";
    }
}