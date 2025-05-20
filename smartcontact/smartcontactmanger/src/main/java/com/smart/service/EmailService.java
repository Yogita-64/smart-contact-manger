package com.smart.service;

import java.util.Properties;

	import org.springframework.stereotype.Service;
	import javax.mail.Authenticator;
	import javax.mail.Message;
	import javax.mail.PasswordAuthentication;
	import javax.mail.Session;
	import javax.mail.Transport;
	import javax.mail.internet.InternetAddress;
	import javax.mail.internet.MimeMessage;
	
	@Service
	public class EmailService {



	public boolean sendEmail(String subject, String message, String to) {
	   boolean f = false;
	   String from = "namratachavan1590@gmail.com";
	   String host = "smtp.gmail.com";

	   // Get system properties
	   Properties properties = System.getProperties();
	   System.out.println("PROPERTIES: " + properties);

	   // Setting important info to properties object
	   properties.put("mail.smtp.host", host);
	   properties.put("mail.smtp.port", "465");
	   properties.put("mail.smtp.ssl.enable", "true");
	   properties.put("mail.smtp.auth", "true");

	   // Get session object
	   Session session = Session.getInstance(properties, new Authenticator() {
	       protected PasswordAuthentication getPasswordAuthentication() {
	           return new PasswordAuthentication("namratachavan1590@gmail.com", "sxol xgui vaoh kguq"); // Use actual password
	       }
	   });

	   session.setDebug(true);

	   MimeMessage m = new MimeMessage(session);
	   try {
	       m.setFrom(from);
	       m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
	       m.setSubject(subject);
	    //   m.setText(message); // Setting the content of the email
	       
	       m.setContent(message,"text/html");

	       Transport.send(m);
	       System.out.println("Sent success............");
	       f = true;
	   } catch (Exception e) {
	       e.printStackTrace();
	   }
	   return f;
	}
	}


