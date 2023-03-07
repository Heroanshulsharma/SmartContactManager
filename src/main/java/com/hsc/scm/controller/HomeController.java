package com.hsc.scm.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hsc.scm.dao.UserRepository;
import com.hsc.scm.helper.Message;
import com.hsc.scm.model.User;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	@Autowired
	private UserRepository userRepository;

	@GetMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home- Smart Contact Manager");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "About- Smart Contact Manager");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register- Smart Contact Manager");
		m.addAttribute("user", new User());
		return "signup";
	}

	@PostMapping("do_register")
	public String register(@Valid @ModelAttribute("user") User user, BindingResult result1,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {

		try {
			if (!agreement) {
				System.out.println("Please agree to terms and conditions");
				throw new Exception("You must agree to terms and conditions");
			}
			if (result1.hasErrors()) {
				model.addAttribute("user", user);
				System.out.println(result1);
				return "signup";
			}

			user.setImageUrl("default.png");
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			model.addAttribute("user", user);
			User result = userRepository.save(user);
			System.out.println(result);
			session.setAttribute("message", new Message("alert-success", "User successfully Registered!!!"));
			return "signup";
			// TODO: handle exception
		} catch (Exception e) {
			// TODO: handle exception
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("alert-danger", "Something Went Wrong!! " + e.getMessage()));
			return "signup";
		}
	}

	@GetMapping("signin")
	public String signIn(Model m) {
		
		m.addAttribute("title","Login- Smart Contact Manager");
		return "signin";
	}
}
