package com.example.dispatch;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
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

    // 配達の月別まとめ
    @GetMapping("/monthly")
    public String monthly(@RequestParam(name = "ym", required = false) String ym,
                          @RequestParam(name = "shift", required = false) Shift shift,
                          Model model) {
        YearMonth target = parseYearMonth(ym);

        List<DispatchRepository.DailyCount> rows =
                repository.sumDaily(target.atDay(1), target.atEndOfMonth(), shift);

        Set<Long> companyIds = new HashSet<>();
        for (DispatchRepository.DailyCount r : rows) {
            if (r.getCompanyId() != null) {
                companyIds.add(r.getCompanyId());
            }
        }
        List<Company> companies = new ArrayList<>(companyRepository.findAllById(companyIds));
        companies.sort(Comparator.comparing(Company::getType)
                .thenComparing(Company::getName));

        model.addAttribute("ym", target);
        model.addAttribute("shift", shift);
        model.addAttribute("summary", new MonthlySummary(target, companies, rows));
        return "dispatches/monthly";
    }

    // 配達の年別まとめ
    @GetMapping("/yearly")
    public String yearly(@RequestParam(name = "year", required = false) Integer year,
                         @RequestParam(name = "view", required = false) String view,
                         Model model) {
        int targetYear = (year == null) ? LocalDate.now().getYear() : year;

        List<DispatchRepository.MonthlyCount> rows = repository.sumMonthly(
                LocalDate.of(targetYear, 1, 1), LocalDate.of(targetYear, 12, 31));

        List<SummaryColumn> columns = new ArrayList<>();
        if ("size".equals(view)) {
            for (VehicleSize s : VehicleSize.values()) {
                columns.add(new SummaryColumn(s.name(), s.getLabel(), null));
            }
        } else if ("item".equals(view)) {
            Set<Long> itemIds = new HashSet<>();
            boolean hasNoItem = false;
            for (DispatchRepository.MonthlyCount r : rows) {
                if (r.getItemId() == null) {
                    hasNoItem = true;
                } else {
                    itemIds.add(r.getItemId());
                }
            }
            List<Item> items = new ArrayList<>(itemRepository.findAllById(itemIds));
            items.sort(Comparator.comparing(Item::getName));
            for (Item i : items) {
                columns.add(new SummaryColumn("i" + i.getId(), i.getName(), null));
            }
            if (hasNoItem) {
                columns.add(new SummaryColumn("i-", "(品目なし)", null));
            }
        } else {
            view = "company";
            Set<Long> companyIds = new HashSet<>();
            for (DispatchRepository.MonthlyCount r : rows) {
                if (r.getCompanyId() != null) {
                    companyIds.add(r.getCompanyId());
                }
            }
            List<Company> companies = new ArrayList<>(companyRepository.findAllById(companyIds));
            companies.sort(Comparator.comparing(Company::getType)
                    .thenComparing(Company::getName));
            for (Company c : companies) {
                columns.add(new SummaryColumn("c" + c.getId(),
                        c.getName() + "(" + c.getType().getLabel() + ")", c.getType()));
            }
        }

        model.addAttribute("year", targetYear);
        model.addAttribute("view", view);
        model.addAttribute("summary", new YearlySummary(targetYear, view, columns, rows));
        return "dispatches/yearly";
    }

    // 「2026-11」の形の文字を年月にする(空や変な形なら今月)
    private YearMonth parseYearMonth(String ym) {
        if (ym == null || ym.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(ym);
        } catch (DateTimeParseException e) {
            return YearMonth.now();
        }
    }

    // 配達入力画面
    @GetMapping("/new")
    public String newForm(Model model) {
        DispatchForm form = new DispatchForm();
        form.setShift(Shift.DAY);
        form.getLines().add(new DispatchLine());
        model.addAttribute("dispatchForm", form);
        return "dispatches/form";
    }

    // 登録処理
    @PostMapping
    public String create(@Valid @ModelAttribute DispatchForm dispatchForm,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {

        Set<VehicleSize> seen = EnumSet.noneOf(VehicleSize.class);
        List<DispatchLine> lines = dispatchForm.getLines();
        for (int i = 0; i < lines.size(); i++) {
            DispatchLine line = lines.get(i);
            if (line == null || line.getVehicleSize() == null) {
                continue;
            }
            if (!seen.add(line.getVehicleSize())) {
                result.rejectValue("lines[" + i + "].vehicleSize", "duplicate",
                        "「" + line.getVehicleSize().getLabel() + "」が2行あります。1行にまとめてください");
            }
        }

        if (result.hasErrors()) {
            return "dispatches/form";
        }

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

    // ↓ 追加:編集画面
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        Dispatch dispatch = findDispatch(id);
        model.addAttribute("dispatchId", id);
        model.addAttribute("dispatchEditForm", DispatchEditForm.from(dispatch));
        return "dispatches/edit";
    }

    // ↓ 追加:更新処理
    @PostMapping("/{id}/edit")
    public String update(@PathVariable("id") Long id,
                         @Valid @ModelAttribute DispatchEditForm dispatchEditForm,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Dispatch dispatch = findDispatch(id);

        if (result.hasErrors()) {
            model.addAttribute("dispatchId", id);
            return "dispatches/edit";
        }

        dispatchEditForm.applyTo(dispatch);
        repository.save(dispatch);

        redirectAttributes.addFlashAttribute("message", "配達を更新しました");
        redirectAttributes.addAttribute("date", dispatch.getDispatchDate().toString());
        return "redirect:/dispatches";
    }

    // ↓ 追加:削除処理
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id,
                         RedirectAttributes redirectAttributes) {
        Dispatch dispatch = findDispatch(id);
        LocalDate date = dispatch.getDispatchDate();

        repository.delete(dispatch);

        redirectAttributes.addFlashAttribute("message", "配達を削除しました");
        redirectAttributes.addAttribute("date", date.toString());
        return "redirect:/dispatches";
    }

    // ↓ 追加:IDで配達を探す(見つからなければ404のエラー画面)
    private Dispatch findDispatch(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
