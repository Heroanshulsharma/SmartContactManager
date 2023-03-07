package com.hsc.scm.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.hsc.scm.dao.ContactRepository;
import com.hsc.scm.dao.UserRepository;
import com.hsc.scm.model.Contact;

@RestController
public class SearchController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@GetMapping("search/{name}")
	public ResponseEntity<?> searchContact(@PathVariable("name") String name, Principal principal){
		
		List <Contact> contacts = this.contactRepository.findByNameContainingAndUser(name,this.userRepository.getUserByUserName(principal.getName()) );
		
		return ResponseEntity.ok(contacts);
		
	}
	
	
}
