package com.hsc.scm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.hsc.scm.dao.UserRepository;
import com.hsc.scm.model.User;

public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub

		User user = userRepository.getUserByUserName(username);

		if(user==null) {
			throw new UsernameNotFoundException("Could not find the user!!!");
		}
		CustomUserDetails customUserDetails= new CustomUserDetails(user);
		
		return customUserDetails;
	}

}