package proj.skybin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// test controller to server login.html and home.html
@Controller
public class TestPageController {
    // server login.html
    @GetMapping("/test/login")
    public String loginPage() {
        return "login";
    }
    // server home.html
    @GetMapping("/test/filetest")
    public String indexPage() {
        return "home";
    }
}
