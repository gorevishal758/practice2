package com.smart.controller;

import javax.servlet.http.HttpSession;

//import javax.validation.Valid;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.MessageHelp;




@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model){

		model.addAttribute("title","Home-Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model){

		model.addAttribute("title","About-Smart Contact Manager");
		return "about";
	}
	@RequestMapping("/signup")
	public String signup(Model model){

		model.addAttribute("title","Signup-Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	//Handler for registering user
	@RequestMapping(value="/do_register",method=RequestMethod.POST)
	public String registerUser( @ModelAttribute("user") User user,BindingResult result1,

			                   @RequestParam(value="agreement",defaultValue="false")
	                            boolean agreement,Model model,HttpSession session)
	{
		
		try {
			if(!agreement)
			{
				System.out.println("you have not agreed the terms and condition");
				throw new Exception("you have not agreed the terms and condition");
			}
			if(result1.hasErrors())
			{
				System.out.println("ERROR"+result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			
			
			user.setRole("ROLE_USER");
			user.setEnable(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement"+ agreement);
			System.out.println("USER"+user);
			
			 User result=this.userRepository.save(user);
			 System.out.println("user information printed :"+result);
			
			model.addAttribute("user", new User()); 
			
			
			
			session.setAttribute("message", new MessageHelp("Successfully registered!!","alert-success"));
			return "signup";
			
			
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			System.out.println("user information printed :"+user);
			session.setAttribute("message",new MessageHelp("Something went Wrong!!" +e.getMessage(), "alert-danger"));
			return "signup";
		}
		
		
	}
	
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login Page");
		return "login";
	}
}
