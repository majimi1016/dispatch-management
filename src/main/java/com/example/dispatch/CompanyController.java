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

@Controller
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyRepository repository;

    public CompanyController(CompanyRepository repository) {
        this.repository = repository;
    }

    // 画面で使う「社内/社外」の選択肢
    @ModelAttribute("companyTypes")
    public CompanyType[] companyTypes() {
        return CompanyType.values();
    }

    // 登録画面と一覧を表示
    @GetMapping
    public String index(Model model) {
        model.addAttribute("company", new Company());
        model.addAttribute("companies", repository.findAllByOrderByNameAsc());
        return "companies/index";
    }

    // 登録処理
    @PostMapping
    public String create(@Valid @ModelAttribute Company company,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        // 同じ会社名がすでにあるかチェック
        if (!result.hasFieldErrors("name") && repository.existsByName(company.getName())) {
            result.rejectValue("name", "duplicate",
                    "「" + company.getName() + "」はすでに登録されています");
        }

        if (result.hasErrors()) {
            model.addAttribute("companies", repository.findAllByOrderByNameAsc());
            return "companies/index";
        }

        repository.save(company);
        redirectAttributes.addFlashAttribute("message",
                "「" + company.getName() + "」を登録しました");
        return "redirect:/companies";
    }
}