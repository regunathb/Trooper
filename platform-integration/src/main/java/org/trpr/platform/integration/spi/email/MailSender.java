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
package org.trpr.platform.integration.spi.email;

import java.net.URL;

/**
 * The <code>MailSender</code> provides methods to send emails to recipients. Specific implementations may use transport and API optimizations, if relevant. 
 * 
 * @author Regunath B
 * @version 1.0, 10/12/2012
 */
public interface MailSender {
	
	/**
	 * Sends an email containing the specified message text to the specified recipients from the specified sender. The attachment is retrieved from the specified valid URL and suitably included
	 * in the message in an implementation specific manner.
	 * Note here that the underlying mail sender (SMTP, Http based API provider etc) is assumed to be initialized already.
	 * @param senderAddress the sender address
	 * @param subject the mail subject
	 * @param recipients the set of recipients for the message
	 * @param message the message text that may have been formatted suitably
	 * @param attachmentURL null or valid URL pointing to the attachment to be included
	 */
	public void sendMail(String senderAddress, String subject, String[] recipients, String message, URL attachmentURL);
	
}
