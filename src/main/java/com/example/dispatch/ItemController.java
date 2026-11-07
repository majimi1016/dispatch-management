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
@RequestMapping("/items")
public class ItemController {

    private final ItemRepository repository;

    public ItemController(ItemRepository repository) {
        this.repository = repository;
    }

    // 登録画面と一覧を表示
    @GetMapping
    public String index(Model model) {
        model.addAttribute("item", new Item());
        model.addAttribute("items", repository.findAllByOrderByNameAsc());
        return "items/index";
    }

    // 登録処理
    @PostMapping
    public String create(@Valid @ModelAttribute Item item,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        // 同じ品目名がすでにあるかチェック
        if (!result.hasFieldErrors("name") && repository.existsByName(item.getName())) {
            result.rejectValue("name", "duplicate",
                    "「" + item.getName() + "」はすでに登録されています");
        }

        if (result.hasErrors()) {
            model.addAttribute("items", repository.findAllByOrderByNameAsc());
            return "items/index";
        }

        repository.save(item);
        redirectAttributes.addFlashAttribute("message",
                "「" + item.getName() + "」を登録しました");
        return "redirect:/items";
    }
}
