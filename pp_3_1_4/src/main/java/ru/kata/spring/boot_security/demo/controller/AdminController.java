package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleServiceImpl;
import ru.kata.spring.boot_security.demo.service.UserServiceImpl;
import ru.kata.spring.boot_security.demo.util.UserValidator;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserServiceImpl userServiceImpl;
    private final UserValidator userValidator;
    private final RoleServiceImpl roleService;

    @Autowired
    public AdminController(UserServiceImpl userServiceImpl, UserValidator userValidator, RoleServiceImpl roleService) {
        this.userServiceImpl = userServiceImpl;
        this.userValidator = userValidator;
        this.roleService = roleService;
    }

    @GetMapping("/")
    public String mainPage(Model model, Principal principal) {
        model.addAttribute("user", userServiceImpl.findByUsername(principal.getName()));
        return "user/profile";
    }
    @GetMapping("/data")
    public String show(@RequestParam(value = "name", required = false) String name,
                       Model model) {
        model.addAttribute("users", userServiceImpl.findAllByUsername(name));
        return "users/data";
    }

    @GetMapping("/new")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        return "users/new";
    }
    @PostMapping("/new")
    public String add(@ModelAttribute("user") @Valid User user, BindingResult bindingResult) {
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return "users/new";
        }
        userServiceImpl.save(user);
        return "redirect:/admin/data?name=";
    }


    @GetMapping("/users/{id}")
    public String showOne(@PathVariable("id") Long id, Model model) {
        List<Role> roles = new ArrayList<>();
        model.addAttribute("user", userServiceImpl.findOne(id));
        model.addAttribute("roles",  roleService.findAll());
        return "users/user";
    }

    @PatchMapping("/users/{id}")
    public String update(@ModelAttribute("user") @Valid User user, BindingResult bindingResult,
                         @PathVariable("id") Long id) {
        if (bindingResult.hasErrors()) {
            return "users/user";
        }
        userServiceImpl.update(id, user);
        return "redirect:/admin/data?name=";
    }

    @DeleteMapping("/users/{id}")
    public String delete(@PathVariable("id") Long id) {
        userServiceImpl.delete(id);
        return "redirect:/admin/data?name=";
    }

}
