package com.hsc.scm.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hsc.scm.model.Contact;
import com.hsc.scm.model.User;

public interface ContactRepository extends JpaRepository<Contact, Integer>{
	
	@Query("from Contact as c where c.user.id=:userId")
	public Page<Contact> findContactsByUserId(@Param("userId")int userId, Pageable pageable);

	@Query("from Contact as c where c.user.id =:userid and c.email =:email order by c.cid desc")
    List<Contact> findByUserAndEmail(@Param("userid") int userid, @Param("email") String email);


	List<Contact> findByNameContainingAndUser(String name, User user);
}
