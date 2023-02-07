package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepositiry;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.MessageHelp;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepositiry contactRepositiry;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName =principal.getName();
		System.out.println("USERNAME "+userName);
		//get the user using username(Email)
		
		User user =userRepository.getUseByUserName(userName);
		System.out.println("USER "+user);
		model.addAttribute("user", user);
	}

	
	//hoe dashboard
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) 
	{
		model.addAttribute("title", "User Dashboard");
		
		return "normal/user_dashboard";
	}
	
//open add form handler
	@GetMapping("/add-contact")
	private String openAddContactForm(Model model) {
		
		model.addAttribute("title", "add contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
		
	}
	
	//process add contact form
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact,
			@RequestParam("profileImage")MultipartFile file,
			Principal principal, HttpSession session) {
		
		
		try {
		String name=principal.getName();
		User user=this.userRepository.getUseByUserName(name);
		
		contact.setUser(user);
		
		//processing and uploading file
		if(file.isEmpty())
		{
			//if the file is empty then try our message
			System.out.println("File is empty");
			contact.setImage("contactlogo.png");
		}
		else {
			//
			contact.setImage(file.getOriginalFilename());
			
		    File saveFile=new ClassPathResource("static/img").getFile();
		    
		    Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
		    
		    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		    
		    System.out.println("Image is uploaded");
		}
		
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("DATA"+contact);
		
		System.out.println("Added to database");
		//message success
		session.setAttribute("message", new MessageHelp("Your contact is added!!...Add more...","success"));
		
		
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Error"+e.getMessage());
			e.printStackTrace();
			//message error
			session.setAttribute("message", new MessageHelp("Something went wrong!! Try again","danger"));

		}
		
		return "normal/add_contact_form";
		
	}
	
	//show contact handeler
	//per page=5[n]
	//current page=0(page)
	
	@GetMapping("show_contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page,Model m,Principal principal) {
		m.addAttribute("title","Show User contacts");
		
		//show contact list
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUseByUserName(userName);
//		  List<Contact> contacts = user.getContacts();
		
		Pageable pageable= PageRequest.of(page, 3);
		
		Page<Contact> contacts =this.contactRepositiry.findContactByUser(user.getId(),pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		
		return "normal/show_contacts";
	}
	
	//Showing perticular contact details
	 @RequestMapping("/{id}/contact")
	 public String showContactDeatails(@PathVariable("id") Integer id,Model model,Principal principal) {
		 
		 
		 System.out.println("CID"+id);
		 
		 Optional<Contact> contactOptional=this.contactRepositiry.findById(id);
		 Contact contact= contactOptional.get();
		 
		 //
		 String userName = principal.getName();
		 User user= this.userRepository.getUseByUserName(userName);
		 
		 if(user.getId()==contact.getUser().getId())
		 model.addAttribute("contact",contact);
		 model.addAttribute("title",contact.getName());
		 
		 return "normal/contact_detail";
	 }
	 
	 //Delete contact handeler
	 @GetMapping("/delete/{id}")
	 public String deleteContact(@PathVariable ("id") Integer id, Model model, HttpSession session) {
		 
		 Optional<Contact> contactOptional=this.contactRepositiry.findById(id);
		 Contact contact=contactOptional.get();
		 
		 //check
		  this.contactRepositiry.delete(contact);
		  System.out.println("DELETE");
		  
		  session.setAttribute("message", new MessageHelp("Contact deleted successfully...","success"));
		 return "redirect:/user/show_contacts/0";
	 }
}
