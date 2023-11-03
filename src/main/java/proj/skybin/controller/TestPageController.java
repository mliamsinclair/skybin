package proj.skybin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestPageController {

    @GetMapping("/test/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/test/filetest")
    public String indexPage() {
        return "home";
    }
}
