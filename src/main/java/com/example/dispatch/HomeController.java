package com.example.dispatch;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // トップページ(/)を開いたら、配達一覧へ移動する
    @GetMapping("/")
    public String home() {
        return "redirect:/dispatches";
    }
}