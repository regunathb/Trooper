/*
 * Copyright 2012-2015, the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trpr.platform.integration.impl.email;

import java.net.URL;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.trpr.platform.integration.spi.email.MailSender;

/**
 * The <code>SpringMailSender</code> is an implementation of the {@link MailSender} interface that uses Spring Java mail classes. 
 * 
 * @author Regunath B
 * @version 1.0, 10/12/2012
 */

public class SpringMailSender implements MailSender {

	/** The JavaMailSender implementation to use */
	private JavaMailSender mailSender;
	
	/**
	 * Interface method implementation. Sends an email using the specified values and the configured mail sender.
	 * @see org.trpr.platform.integration.spi.email.MailSender#sendMail(java.lang.String, java.lang.String[], java.lang.String, java.net.URL)
	 */
	public void sendMail(final String senderAddress,  final String subject, final String[] recipients, final String message, final URL attachmentURL) {
	    MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
            	InternetAddress[] recipientAddresses = new InternetAddress[recipients.length];
            	for (int i = 0; i < recipientAddresses.length; i++) {
            		recipientAddresses[i] = new InternetAddress(recipients[i]);
            	}
                mimeMessage.setRecipients(Message.RecipientType.TO, recipientAddresses);
                mimeMessage.setFrom(new InternetAddress(senderAddress));
                mimeMessage.setSubject(subject);
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); // multi-part flag is set to true for accommodating attachments
                if (attachmentURL != null) {
                	helper.addAttachment(attachmentURL.getFile(), new FileSystemResource(attachmentURL.toString()));
                }
                helper.setText(message);
            }
        };		
        this.mailSender.send(preparator);
	}
	
	/** Getter/Setter methods*/
	public JavaMailSender getMailSender() {
		return this.mailSender;
	}
	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	/** End Getter/Setter methods*/
	

}
