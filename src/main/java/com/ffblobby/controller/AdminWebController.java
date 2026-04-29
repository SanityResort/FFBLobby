package com.ffblobby.controller;

import com.ffblobby.dto.BackendServerRequest;
import com.ffblobby.service.BackendServerService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final BackendServerService backendServerService;

    public AdminWebController(BackendServerService backendServerService) {
        this.backendServerService = backendServerService;
    }

    @GetMapping
    public String adminPage(Model model) {
        model.addAttribute("backends", backendServerService.findAll());
        model.addAttribute("request", new BackendServerRequest());
        return "admin";
    }

    @PostMapping("/backends")
    public String register(@ModelAttribute BackendServerRequest request,
                           RedirectAttributes redirectAttributes) {
        try {
            backendServerService.register(request);
            redirectAttributes.addFlashAttribute("success", "Backend registered successfully.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Backend with name '" + request.getName() + "' already exists.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/backends/{name}/delete")
    public String deregister(@PathVariable String name, RedirectAttributes redirectAttributes) {
        try {
            backendServerService.deregister(name);
            redirectAttributes.addFlashAttribute("success", "Backend deregistered successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/backends/{name}/primary")
    public String setPrimary(@PathVariable String name, RedirectAttributes redirectAttributes) {
        try {
            backendServerService.setPrimary(name);
            redirectAttributes.addFlashAttribute("success",
                    "Backend '" + name + "' set as primary.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin";
    }
}
