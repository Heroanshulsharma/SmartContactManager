package com.hsc.scm.controller;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hsc.scm.dao.UserRepository;
import com.hsc.scm.helper.Message;
import com.hsc.scm.model.User;
import com.hsc.scm.services.EmailService;

@Controller
public class ForgotController {

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping("forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	@PostMapping("send-otp")
	public String sendOtp(@RequestParam("email") String email, HttpSession session) {
		
		
		User user = this.userRepository.getUserByUserName(email);
		if(user==null) {
			session.setAttribute("message", new Message("alert-danger", "User with this email address is not registered with us"));
			return "forgot_email_form";
		}
		
		int otp= ThreadLocalRandom.current().nextInt(100000, 1000000);
		
		String subject="Smart Contact Manager - Account Verification";
		String message="<h3>"
				+ "<b>"
				+ "OTP:"
				+ otp
				+ "</b>"
				+ "</h3>";
		String to = email;
		boolean flag =this.emailService.sendEmail(to, subject, message);		
		
		if(flag) {
			session.setAttribute("generatedOtp", otp);
			session.setAttribute("email", email);
			session.setAttribute("message", new Message("alert-success", "OTP has been sent successfully"));
			return "verify_otp";
		}
		else {
			session.setAttribute("message", new Message("alert-danger", "Invalid email"));
			return "forgot_email_form";
		}
		
	}
	
	@PostMapping("verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		
		int generatedOtp =(int) session.getAttribute("generatedOtp");
		
		if(otp == generatedOtp) {
			
			String email= (String) session.getAttribute("email");
			User user = this.userRepository.getUserByUserName(email);
			if(user!=null) {
				
				return "change_password";
			}
			else{
				session.setAttribute("message", new Message("alert-danger", "Invalid OTP"));
				return "forgot_email_form";
			}
			
			
		}
		else {
			session.setAttribute("message", new Message("alert-danger", "You have entered invalid otp"));
			return "verify_otp";
		}
		
		
		
	}
	
	@PostMapping("change-password")
	public String changePwd(@RequestParam("newPassword")String newPassword, HttpSession session) {
		String email= (String) session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.passwordEncoder.encode(newPassword));
		this.userRepository.save(user);
		return "redirect:/signin?change= Password Changed Successfully";
		
		
	}
	
}
