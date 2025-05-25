package com.example.livetask.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.livetask.form.RegisterForm;
import com.example.livetask.model.User;
import com.example.livetask.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;


@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("form") @Valid RegisterForm form,
                           BindingResult result,
                           Model model,
                           Locale locale) {

        if (result.hasErrors()) {
            return "register";
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            String message = messageSource.getMessage("register.password.mismatch", null, locale);
            model.addAttribute("error", message);
            model.addAttribute("form", form);
            return "register";
        }

        if (userRepository.existsByEmail(form.getEmail())) {
            String message = messageSource.getMessage("register.duplicate", null, locale);
            model.addAttribute("error", message);
            model.addAttribute("form", form);
            return "register";
        }

        User user = new User();
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        userRepository.save(user);

        return "redirect:/login?registered";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }
}
