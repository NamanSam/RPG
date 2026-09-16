package com.codequest.learning;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api")
public class LearningController {
    private final LearningService service;
    public LearningController(LearningService service) { this.service=service; }
    public record Answer(@NotBlank @Size(max=2000) String answer) {}
    @GetMapping("/campaigns") public Object campaigns(Authentication a) { return service.campaigns((Long)a.getPrincipal()); }
    @GetMapping("/campaigns/dsa/map") public Object map(Authentication a) { return service.map((Long)a.getPrincipal()); }
    @GetMapping("/topics/{topic}/progress") public Object progress(Authentication a,@PathVariable String topic) { return service.progress((Long)a.getPrincipal(),topic); }
    @GetMapping("/topics/{topic}/trails/{number}") public Object trail(Authentication a,@PathVariable String topic,@PathVariable int number) { return service.trail((Long)a.getPrincipal(),topic,number); }
    @GetMapping("/questions/{id}") public Object detail(Authentication a,@PathVariable String id) { return service.detail((Long)a.getPrincipal(),id); }
    @PostMapping("/questions/{id}/submit") public Object submit(Authentication a,@PathVariable String id,@Valid @RequestBody Answer answer) { return service.submit((Long)a.getPrincipal(),id,answer.answer()); }
}
