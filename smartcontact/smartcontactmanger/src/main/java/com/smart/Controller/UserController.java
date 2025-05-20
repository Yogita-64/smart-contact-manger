package com.smart.Controller;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entites.Contact;
import com.smart.entites.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private BCryptPasswordEncoder  bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal)
	{
		
		 String userName = principal.getName();
	       System.out.println("USERNAME:"+userName);
	       
	       
	    // get the user using username(Email)    
	       
	         User user= userRepository.getUserByUserName(userName);
	         
	         System.out.println("USER"+user);
	         
	         model.addAttribute("user",user);
	       
		
	}
	
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard( Model model,Principal principal)
	{
		      
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	

	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage")  MultipartFile file , Principal principal,HttpSession session) {
		
		try {
		
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
		//processing and uploading file
		if(file.isEmpty()) {
			// if the file is empty then try your msg
			System.out.println("File is empty");
			contact.setImage("contacts.png");
			
		}else {
			// upload the file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			
			File saveFile =new ClassPathResource("static/img").getFile();
			
			 Path path =Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			 
			Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING );
			
			System.out.println("Image is Uploaded");
		}
		
		contact.setUser(user);
		
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		
		System.out.println("DATA "+contact);
		System.out.println("Added to Database");
		
		// message success........
		
		session.setAttribute("message", new Message("Your contact is Added !! Add new more..", "success"));
		
	}catch 
	 (Exception e) {
		System.out.println("ERROR"+e.getMessage());
		e.printStackTrace();
		
		// Error message
		
		session.setAttribute("message", new Message("Somethinf went wrong !! Try again.....", "danger"));
	}
		return "normal/add_contact_form";
	}


	
	// show all contacts handler 
	
	//per page =5[n]
	//current page = 0[page]
	
	@GetMapping("/show_contacts/{page}")
	public String showContacts( @PathVariable("page") Integer page ,Model model,Principal principal) {
		model.addAttribute("title","show User Contacts");
		// we have to send all contact list 
		
		
		 String userName= principal.getName();
			User user = this.userRepository.getUserByUserName(userName);
			
			// currentPage-page
			//Contact per page-5
		     Pageable pageable= PageRequest.of(page, 3);
			
			
		  Page<Contact> contacts= this.contactRepository.findContactsByUser(user.getId(), pageable);
		 model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		/*
		 * String userName =principal.getName(); User user =
		 * this.userRepository.getUserByUserName(userName); List<Contact> contacts=
		 * user.getContacts();
		 */
		
		  
		return "normal/show_contacts";
	}
	
	
	// Showing particular contact detail
	
	
	
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal)
	{
		System.out.println("CID" +cId);
		
		 Optional<Contact> contactOptional= this.contactRepository.findById(cId);
		     Contact contact = contactOptional.get();
		     
		     
		     //
		 String username = principal.getName();
		User user=this.userRepository.getUserByUserName(username);
		
		if(user.getId()==contact.getUser().getId())
		{
		   model.addAttribute("contact",contact);
		   model.addAttribute("title",contact.getName());
		}
		     
		    
		     
		return "normal/contactDetail";
	}
	
	// Delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session,Principal principal)
	{
     Optional<Contact> contactOptional= this.contactRepository.findById(cId);
     
     Contact contact= contactOptional.get();
     
     //check....assignment
     //delete photo
     System.out.println("Contact"+contact.getcId());
     
     
    // contact.setUser(null);
     //REMOVE
     //IMG
     
     //
     
     User user= this.userRepository.getUserByUserName(principal.getName());
     user.getContacts().remove(contact);
     this.userRepository.save(user);
     
      session.setAttribute("message", new Message("Contact Deleted Succsesfully!...", "success"));
		return "redirect:/user/show_contacts/0";
	}
	
	
	//Open update form handler
	@PostMapping("/update-contact/{cid}")
	
	public String updateForm(@PathVariable("cid") Integer cid,  Model m)
	{
		m.addAttribute("title", "Update contact");
		   Contact  contact = this.contactRepository.findById(cid).get();
		   m.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//Update Contact Handler
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact  contact , @RequestParam("profileImage")MultipartFile file,
			Model m,HttpSession session,Principal principal) {
		
		
		try {
			//old contact details
		Contact oldcontactDetail =	this.contactRepository.findById(contact.getcId()).get();
			//image
			if( !file.isEmpty())
			{
				//file work
				//rewrite
				// delete old photo
				
				File deleteFile =new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldcontactDetail.getImage());
				file1.delete();
				
		
				 
				
				// Update new photo
				
				
				File saveFile =new ClassPathResource("static/img").getFile();
				
				 Path path =Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				 
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING );
				
				contact.setImage(file.getOriginalFilename());
				
			}else
			{
				contact.setImage(oldcontactDetail.getImage());
			}
			
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact is Updated","success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("CONTACT NAME"+contact.getName());
		System.out.println("CONTACT ID"+contact.getcId());
		return"redirect:/user/"+contact.getcId()+"/contact";
	}
	
	// Your Profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	//Open Settings Handler
	@GetMapping("/settings")
	public String openSettings()
	{
		return "normal/settings";
	}
	
	//change Password...handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session)
	{
		
		System.out.println("OLD PASSWORD"+oldPassword);
		System.out.println("NEW PASSWORD"+newPassword);
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			//change the Password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your Password Successfully changed..","success"));
			
			
		}else
		{
			//error..
			
			session.setAttribute("message", new Message("Please Enter correct old password !!","danger"));
			return "redirect:/user/settings";
			
		}
		return "redirect:/user/index";
	}
	
}
