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

import org.mule.api.transaction.TransactionException;
import org.mule.config.i18n.CoreMessages;
import org.mule.transaction.AbstractSingleResourceTransaction;

/**
 * The <code>RabbitTransaction</code> is a sub-type of the Mule {@link AbstractSingleResourceTransaction} that is available just for implementation completeness.
 * 
 * This TX implementation is not being used in the Mule transport implementation. All TX related calls on the {@link RabbitTransactionFactory} throw exceptions to 
 * indicate lack of support for TX defined via Mule.
 * 
 * @author Regunath B
 * @version 1.0, 17/08/2012
 */
public class RabbitTransaction extends AbstractSingleResourceTransaction {
	
	/**
	 * Overriden method that throws a {@link TransactionException} to indicate that Mule transactions are not supported!
	 * @see org.mule.transaction.AbstractTransaction#doBegin()
	 */
    protected void doBegin() throws TransactionException {
        //no support for TX defined via Mule
    	throw new TransactionException(CoreMessages.cannotStartTransaction("Transactions are not supported on this Mule RabbitMQ transport implementation! Cannot start a TX via Mule"));
    }

    /**
	 * Overriden method that throws a {@link TransactionException} to indicate that Mule transactions are not supported!
     * @see org.mule.transaction.AbstractTransaction#doCommit()
     */
    protected void doCommit() throws TransactionException {
        //no support for TX defined via Mule
    	throw new TransactionException(CoreMessages.cannotStartTransaction("Transactions are not supported on this Mule RabbitMQ transport implementation! Cannot start a TX via Mule"));
    }

    /**
     * Overriden method that throws a {@link TransactionException} to indicate that Mule transactions are not supported!
     * @see org.mule.transaction.AbstractTransaction#doRollback()
     */
    protected void doRollback() throws TransactionException  {
        //no support for TX defined via Mule
    	throw new TransactionException(CoreMessages.cannotStartTransaction("Transactions are not supported on this Mule RabbitMQ transport implementation! Cannot start a TX via Mule"));
    }
}
