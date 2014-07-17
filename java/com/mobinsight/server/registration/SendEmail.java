package com.mobinsight.server.registration;

import java.io.IOException;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;

@Singleton
public class SendEmail {

	static final Logger LOG = Logger.getLogger(SendEmail.class.getSimpleName());
	private String fromAddress;
	
	@Inject
	public SendEmail(@Named("email.from") String fromAddress) {
		this.fromAddress = fromAddress;
	}
	
	public void send(String to, String title, String htmlMessage) {
		MailService mailService = MailServiceFactory.getMailService();
		MailService.Message message = new MailService.Message();
		message.setSender(fromAddress);
		message.setTo(to);
		message.setSubject(title);
		message.setHtmlBody(htmlMessage);
		
		try {
			mailService.send(message);
			LOG.info("Registration code sent to" + to);
		} catch (IOException e) {
			LOG.warning("Could not send registration code to " + to);
			e.printStackTrace();
		}
	}
}
