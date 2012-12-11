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
package org.trpr.example.batch.email;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.trpr.platform.integration.spi.email.MailSender;


/**
 * The <code>EmailTasklet</code> class is a simple implementation of the {@link Tasklet} that sends out an email using the injected {@link MailSender} implementation.
 * 
 * @author Regunath B
 * @version 1.0, 11 Dec 2012
 */
public class EmailTasklet implements Tasklet {
	
	/** The MailSender implementation */
	private MailSender mailSender;

	/**
	 * Interface method implementation. Sends out a canned email.
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.springframework.batch.core.StepContribution, org.springframework.batch.core.scope.context.ChunkContext)
	 */
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
		this.mailSender.sendMail("noreply@trooper.org", "Trooper Batch : status", new String[]{"regunathb@trooper.org"}, 
				"Email tasklet run successful!", null);
		return RepeatStatus.FINISHED;
	}
	
	/** Getter/Setter methods*/
	public MailSender getMailSender() {
		return this.mailSender;
	}
	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}
	

}
