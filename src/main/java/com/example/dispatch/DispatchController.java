package com.example.dispatch;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/dispatches")
public class DispatchController {

    private final DispatchRepository repository;
    private final CompanyRepository companyRepository;   // ← 追加

    public DispatchController(DispatchRepository repository,
                              CompanyRepository companyRepository) {   // ← 変更
        this.repository = repository;
        this.companyRepository = companyRepository;      // ← 追加
    }

    // 会社のプルダウンの選択肢(登録済みの会社)  ← 追加
    @ModelAttribute("companies")
    public List<Company> companies() {
        return companyRepository.findAllByOrderByNameAsc();
    }

    // 車格のプルダウンの選択肢  ← 追加
    @ModelAttribute("vehicleSizes")
    public VehicleSize[] vehicleSizes() {
        return VehicleSize.values();
    }

    // 一覧画面
    @GetMapping
    public String list(Model model) {
        model.addAttribute("dispatches", repository.findAllByOrderByDispatchDateAsc());
        return "dispatches/list";
    }

    // 登録画面
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("dispatch", new Dispatch());
        return "dispatches/form";
    }

    // 登録処理
    @PostMapping
    public String create(@Valid @ModelAttribute Dispatch dispatch, BindingResult result) {
        if (result.hasErrors()) {
            return "dispatches/form";
        }
        repository.save(dispatch);
        return "redirect:/dispatches";
    }
}