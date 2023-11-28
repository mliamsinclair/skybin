package proj.skybin.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// test controller to server login.html and home.html
// change to @RestController to test file and folder creation
@Controller
public class PageController {

    // re-serve index.html
    @GetMapping("/account")
    public String account() {
        return "index";
    }

    // re-serve index.html
    @GetMapping("/about")
    public String about() {
        return "index";
    }

}
