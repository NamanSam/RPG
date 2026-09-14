package com.codequest.quest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api")
public class QuestController {
    private final QuestService quests;
    public QuestController(QuestService quests) { this.quests = quests; }
    public record Answer(@NotBlank @Size(max = 80) String answer) {}
    @GetMapping("/me/progress")
    public QuestService.Progress progress(Authentication auth) { return quests.progress((Long) auth.getPrincipal()); }
    @GetMapping("/quests/{id}")
    public QuestService.QuestView detail(Authentication auth, @PathVariable String id) { return quests.detail((Long) auth.getPrincipal(), id); }
    @PostMapping("/quests/{id}/submit")
    public QuestService.Submission submit(Authentication auth, @PathVariable String id, @Valid @RequestBody Answer body) {
        return quests.submit((Long) auth.getPrincipal(), id, body.answer());
    }
}
