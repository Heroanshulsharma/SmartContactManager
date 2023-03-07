package com.hsc.scm.controller;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hsc.scm.dao.ContactRepository;
import com.hsc.scm.dao.UserRepository;
import com.hsc.scm.helper.Message;
import com.hsc.scm.model.Contact;
import com.hsc.scm.model.User;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	BCryptPasswordEncoder passwordEncoder;

	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String username = principal.getName();
		User user = userRepository.getUserByUserName(username);
		m.addAttribute("user", user);
	}

	// User's home page or dashboard
	@GetMapping("index")
	public String index(Model m, Principal prinicipal) {
		m.addAttribute("title", "User Dashboard");
		return "Normal/user";
	}

	// Add Contact form
	@GetMapping("add-contact")
	public String addContact(Model m) {
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		return "Normal/add_contact";
	}

	// To validate and save contacts in database
	@PostMapping("process-contact")
	public String addContact(@Valid @ModelAttribute Contact contact, BindingResult result, Model m, Principal principal,
			HttpSession session, @RequestParam("profileImage") MultipartFile file) {
		try {
			if (result.hasErrors()) {
				System.out.println(result);
				m.addAttribute("contact", contact);
				return "Normal/add_contact";
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setImage("profile.jpg");
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);

			if (!file.isEmpty()) {

				Contact contactSaved = this.contactRepository.findByUserAndEmail(user.getId(), contact.getEmail())
						.get(0);
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + contactSaved.getCid() + "_"
						+ file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contactSaved.setImage(contactSaved.getCid() + "_" + file.getOriginalFilename());
				this.contactRepository.save(contactSaved);
			}

			session.setAttribute("message", new Message("alert-success", "Contact Saved successfully"));

			return "Normal/add_contact";
		} catch (Exception e) {
			session.setAttribute("message", new Message("alert-danger", e.getMessage()));
			return "Normal/add_contact";
		}
	}

	// To Show all contacts
	@GetMapping("show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {

		String email = principal.getName();
		User user = this.userRepository.getUserByUserName(email);
		Pageable pageable = PageRequest.of(page, 3);

		Page<Contact> contacts = this.contactRepository.findContactsByUserId(user.getId(), pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("totalPages", contacts.getTotalPages());
		m.addAttribute("currentPage", page);

		return "Normal/show-contacts";
	}

	// To select a particular contact

	@GetMapping("{cid}/contact")
	public String contact(@PathVariable("cid") Integer id, Model m, Principal principal) {

		try {
			Optional<Contact> contactOptional = this.contactRepository.findById(id);
			Contact contact = contactOptional.get();

			if (contact.getUser().getId() == (this.userRepository.getUserByUserName(principal.getName()).getId()))
				m.addAttribute("contact", contact);

			return "Normal/contact_details";
		} catch (Exception e) {
			return "Normal/contact_details";
		}
	}

	// To delete a particular contact

	@GetMapping("delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid, Model m, Principal principal, HttpSession session) {

		try {
			Contact contact = this.contactRepository.findById(cid).get();

			if (contact.getUser().getId() == this.userRepository.getUserByUserName(principal.getName()).getId()) {
				contact.setUser(null);
				this.contactRepository.delete(contact);
				session.setAttribute("message", new Message("alert-success", "Contact deleted successfully"));
			} else {
				session.setAttribute("message",
						new Message("alert-danger", "You don't have permission to delete this contact"));
			}

			return "redirect:/user/show-contacts/0";
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			session.setAttribute("message",
					new Message("alert-danger", "You don't have permission to delete this contact"));

			return "redirect:/user/show-contacts/0";
		}
	}

	// To open the page to update a particular contact
	
	@PostMapping("update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid, Model m) {
		Contact contact = this.contactRepository.findById(cid).get();
		m.addAttribute("title", "Update" + contact.getName());
		m.addAttribute("contact", contact);

		return "Normal/update-contact";
	}

	// To process the update contact request
	
	@PostMapping("process-contact-update")
	public String updateContact(@Valid @ModelAttribute Contact contact, BindingResult result, Model m,
			Principal principal, HttpSession session, @RequestParam("profileImage") MultipartFile file) {
		try {
			if (result.hasErrors()) {
				System.out.println(result);
				m.addAttribute("contact", contact);
				return "Normal/update-contact";
			}
			Contact oldContact = this.contactRepository.findById(contact.getCid()).get();

			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			contact.setImage(oldContact.getImage());

			if (!file.isEmpty()) {

				File oldFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(oldFile.getAbsolutePath() + File.separator + oldContact.getImage());
				Files.deleteIfExists(path);

				File newFile = new ClassPathResource("static/img").getFile();
				path = Paths.get(newFile.getAbsolutePath() + File.separator + oldContact.getCid() + "_"
						+ file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(oldContact.getCid() + "_" + file.getOriginalFilename());
			}
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("alert-success", "Contact Updated successfully"));

			
		} catch (Exception e) {
			session.setAttribute("message", new Message("alert-danger", e.getMessage()));
			
		}
		return "redirect:/user/"+contact.getCid()+"/contact";
	}

	// To open user profile
	
	@GetMapping("profile")
	public String userProfile(Model m) {
		m.addAttribute("title", "Profile page");
		return "Normal/profile";
	}

	// To open dashboard
	
	@GetMapping("/dashboard")
	public String dashboard(Model m) {
		m.addAttribute("title", "Home");
		return "Normal/dashboard";
	}

	
	//To open settings tab for change password module
	@GetMapping("settings")
	public String settings() {
		return "Normal/settings";
	}
	
	@PostMapping("changepwd")
	public String changepwd(@RequestParam("oldpwd") String oldPwd, @RequestParam("newpwd") String newpwd, Principal principal, HttpSession session) {
		
		
		
		System.out.println(oldPwd);
		System.out.println(newpwd);
		
		String username = principal.getName();
		User currentUser= this.userRepository.getUserByUserName(username);
		
		if(this.passwordEncoder.matches(oldPwd, currentUser.getPassword())) {
			
			currentUser.setPassword(this.passwordEncoder.encode(newpwd));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("alert-success", "Your password is changed successfully."));
			
		}else {
			session.setAttribute("message", new Message("alert-danger", "Please enter the correct old password!!!"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
}
