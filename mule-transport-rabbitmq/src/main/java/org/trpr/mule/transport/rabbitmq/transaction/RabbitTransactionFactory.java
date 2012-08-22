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
 * 
 * This implementation is loosely based on the (now defunct) RabbitMQ transport codebase for Mule (version 1.x.x and above) using the 
 * RabbitMQ Java client (version 1.7.1) : http://svn.muleforge.org/mule-transport-rabbitmq/branches/upgrading-to-rabbitmq-client-1.7/
 */

package org.trpr.mule.transport.rabbitmq.transaction;

import org.mule.api.MuleContext;
import org.mule.api.transaction.Transaction;
import org.mule.api.transaction.TransactionException;
import org.mule.api.transaction.TransactionFactory;
import org.mule.config.i18n.CoreMessages;

/**
 * The <code>RabbitTransactionFactory</code> is an implementation for {@link TransactionFactory} that is available just for implementation completeness.
 * 
 * Marking this as being non-transacted i.e. cannot participate in Mule defined transactions.
 * Throws a TransactionException when any TX related calls are made on this TransactionFactory 
 * 
 * @author Regunath
 * @version 1.0, 17/08/2012
 */
public class RabbitTransactionFactory implements TransactionFactory {
	
	/**
	 * Interface method implementation. Throws a {@link TransactionException} to indicate that Mule transactions are not supported!
	 * @see org.mule.api.transaction.TransactionFactory#beginTransaction(org.mule.api.MuleContext)
	 */
    public Transaction beginTransaction(MuleContext context) throws TransactionException {    
    	// no support for TX
    	throw new TransactionException(CoreMessages.cannotStartTransaction("Transactions are not supported on this Mule RabbitMQ transport implementation! Cannot start a TX via Mule"));
    }

    /**
     * Interface method implementation. Always returns boolean FALSE to indicate that this TransactionFactory is non-transactional 
     * @see org.mule.api.transaction.TransactionFactory#isTransacted()
     */
    public boolean isTransacted() {
    	// this TX factory is not transacted
    	return false;
    }
}
