package proj.skybin.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class PageController {

    @GetMapping("/account")
    public RedirectView account() {
        return new RedirectView("/");
    }

    @GetMapping("/about")
    public RedirectView about() {
        return new RedirectView("/");
    }

    @GetMapping("/login")
    public RedirectView login() {
        return new RedirectView("/");
    }

}