package com.example.dispatch;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dispatches")
public class DispatchController {

    private final DispatchRepository repository;

    public DispatchController(DispatchRepository repository) {
        this.repository = repository;
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