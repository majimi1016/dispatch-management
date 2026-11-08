package com.example.dispatch;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // 配達一覧(日付の指定がなければ今日)
    @GetMapping
    public String list(@RequestParam(name = "date", required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       Model model) {
        if (date == null) {
            date = LocalDate.now();
        }

        List<Dispatch> dispatches =
                repository.findByDispatchDateOrderByShiftAscCompanyNameAscIdAsc(date);

        // 車格ごとの合計台数を数える
        Map<VehicleSize, Integer> sizeTotals = new EnumMap<>(VehicleSize.class);
        int grandTotal = 0;
        for (Dispatch d : dispatches) {
            if (d.getVehicleSize() != null && d.getQuantity() != null) {
                sizeTotals.merge(d.getVehicleSize(), d.getQuantity(), Integer::sum);
                grandTotal += d.getQuantity();
            }
        }

        model.addAttribute("date", date);
        model.addAttribute("dispatches", dispatches);
        model.addAttribute("sizeTotals", sizeTotals);
        model.addAttribute("grandTotal", grandTotal);
        return "dispatches/list";
    }

    // 配達入力画面
    @GetMapping("/new")
    public String newForm(Model model) {
        DispatchForm form = new DispatchForm();
        form.setShift(Shift.DAY);                // 最初は「昼」
        form.getLines().add(new DispatchLine()); // 車格の行を1行用意しておく
        model.addAttribute("dispatchForm", form);
        return "dispatches/form";
    }

    // 登録処理
    @PostMapping
    public String create(@Valid @ModelAttribute DispatchForm dispatchForm,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {

        // ↓ 追加:同じ車格が2行以上ないかチェック
        Set<VehicleSize> seen = EnumSet.noneOf(VehicleSize.class);
        List<DispatchLine> lines = dispatchForm.getLines();
        for (int i = 0; i < lines.size(); i++) {
            DispatchLine line = lines.get(i);
            if (line == null || line.getVehicleSize() == null) {
                continue;  // 車格が未選択の行は、別のチェックでエラーになるので飛ばす
            }
            if (!seen.add(line.getVehicleSize())) {
                result.rejectValue("lines[" + i + "].vehicleSize", "duplicate",
                        "「" + line.getVehicleSize().getLabel() + "」が2行あります。1行にまとめてください");
            }
        }

        if (result.hasErrors()) {
            return "dispatches/form";
        }

        // 車格の行ごとに、配達を1件ずつ作る
        List<Dispatch> dispatches = new ArrayList<>();
        for (DispatchLine line : lines) {
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
        redirectAttributes.addAttribute("date", dispatchForm.getDispatchDate().toString());
        return "redirect:/dispatches";
    }
}