package org.sword;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class PluginController1Impl implements IPluginController {

    @GetMapping
    public String hello() {
        return "hello";
    }
}
