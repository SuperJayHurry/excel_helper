package org.example.controller;

import java.security.Principal;
import org.example.service.SubmissionService;
import org.example.service.TemplateService;
import org.example.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class TeacherController {

    private final SubmissionService submissionService;
    private final TemplateService templateService;
    private final UserService userService;

    public TeacherController(SubmissionService submissionService,
                             TemplateService templateService,
                             UserService userService) {
        this.submissionService = submissionService;
        this.templateService = templateService;
        this.userService = userService;
    }

    @GetMapping("/teacher/dashboard")
    public String dashboard(Principal principal, Model model) {
        var user = userService.findByUsername(principal.getName());
        model.addAttribute("assignments", submissionService.assignmentsFor(user.getId()));
        return "teacher/dashboard";
    }

    @PostMapping("/teacher/templates/{id}/submit")
    public String submit(@PathVariable Long id,
                         Principal principal,
                         @RequestParam("submissionFile") MultipartFile file) {
        var user = userService.findByUsername(principal.getName());
        submissionService.submit(id, user, file);
        return "redirect:/teacher/dashboard?submitted";
    }

    @GetMapping("/teacher/templates/{id}")
    public String templatePreview(@PathVariable Long id, Model model) {
        model.addAttribute("task", templateService.getById(id));
        model.addAttribute("recipients", templateService.findRecipients(id));
        model.addAttribute("submissions", submissionService.findByTemplate(id));
        model.addAttribute("aggregation", templateService.latestAggregation(id));
        return "admin/template-detail";
    }
}

