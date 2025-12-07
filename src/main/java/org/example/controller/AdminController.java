package org.example.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import org.example.dto.TemplateForm;
import org.example.entity.User;
import org.example.entity.enums.Department;
import org.example.service.SubmissionService;
import org.example.service.TemplateService;
import org.example.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdminController {

    private final TemplateService templateService;
    private final UserService userService;
    private final SubmissionService submissionService;

    public AdminController(TemplateService templateService,
                           UserService userService,
                           SubmissionService submissionService) {
        this.templateService = templateService;
        this.userService = userService;
        this.submissionService = submissionService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("templateForm", new TemplateForm());
        model.addAttribute("departments", Department.values());
        model.addAttribute("teachers", userService.findTeachers());
        model.addAttribute("tasks", templateService.findAll());
        return "admin/dashboard";
    }

    @PostMapping("/admin/templates")
    public String createTemplate(@Valid @ModelAttribute("templateForm") TemplateForm form,
                                 BindingResult bindingResult,
                                 @RequestParam("templateFile") MultipartFile templateFile,
                                 Principal principal,
                                 Model model) {
        if (bindingResult.hasErrors() || templateFile.isEmpty()) {
            model.addAttribute("departments", Department.values());
            model.addAttribute("teachers", userService.findTeachers());
            model.addAttribute("tasks", templateService.findAll());
            model.addAttribute("fileError", templateFile.isEmpty() ? "请上传模板文件" : null);
            return "admin/dashboard";
        }
        User admin = userService.findByUsername(principal.getName());
        templateService.createTask(form, templateFile, admin);
        return "redirect:/admin/dashboard?success";
    }

    @GetMapping("/admin/templates/{id}")
    public String templateDetail(@PathVariable Long id, Model model) {
        model.addAttribute("task", templateService.getById(id));
        model.addAttribute("recipients", templateService.findRecipients(id));
        model.addAttribute("submissions", submissionService.findByTemplate(id));
        model.addAttribute("aggregation", templateService.latestAggregation(id));
        return "admin/template-detail";
    }

    @PostMapping("/admin/templates/{id}/remind")
    public String remind(@PathVariable Long id,
                         @RequestParam(defaultValue = "请尽快上传汇总表") String message) {
        templateService.remindPending(id, message);
        return "redirect:/admin/templates/" + id + "?reminded";
    }

    @PostMapping("/admin/templates/{id}/aggregate")
    public String aggregate(@PathVariable Long id) {
        templateService.aggregate(id);
        return "redirect:/admin/templates/" + id + "?aggregated";
    }
}

