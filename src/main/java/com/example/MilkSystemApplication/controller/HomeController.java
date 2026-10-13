package com.example.MilkSystemApplication.controller;

import com.example.MilkSystemApplication.model.AdminUser;
import com.example.MilkSystemApplication.model.AppSetting;
import com.example.MilkSystemApplication.service.AdminUserService;
import com.example.MilkSystemApplication.service.FarmerService;
import com.example.MilkSystemApplication.service.MilkCollectionService;
import com.example.MilkSystemApplication.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private MilkCollectionService milkService;

    @Autowired
    private FarmerService farmerService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        AppSetting settings = milkService.getSettings();
        model.addAttribute("settings", settings);
        model.addAttribute("totalFarmers", farmerService.getTotalFarmersCount());
        model.addAttribute("totalLitres", milkService.getTotalLitres());
        model.addAttribute("totalPaid", paymentService.getTotalPaid());
        model.addAttribute("rateFactor", milkService.getRateFactor());
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        AppSetting settings = milkService.getSettings();
        model.addAttribute("settings", settings);
        return "login";
    }

    @PostMapping("/signup")
    public String registerAdmin(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String societyName,
            RedirectAttributes redirectAttributes) {

        try {
            AdminUser admin = adminUserService.registerAdmin(username, password, fullName, email, phone, societyName);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Welcome " + admin.getFullName() + "! Your Dairy Society admin account is registered with a 15-Day Free Trial! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/?signupError=true";
        }
    }
}
