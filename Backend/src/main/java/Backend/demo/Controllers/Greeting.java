package Backend.demo.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Greeting {

    @GetMapping("/")
    public String greet() {
        return "Hello, World!";
    }
}
