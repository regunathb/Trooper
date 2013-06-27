package org.trpr.platform.integration.impl.messaging;

import org.trpr.platform.integration.spi.messaging.MessagePublisher;
import org.trpr.platform.integration.spi.messaging.MessagingException;

/**
 * The <code>RabbitMQMessagePublisherImpl</code> class is an implementation of {@link MessagePublisher} that uses
 * RabbitMQ as the underlying messaging
 * provider. This implementation is compatible with Java client API of RabbitMQ version 2.2.0. Backward/Forward
 * compatibility with other versions
 * requires verification.
 * This publisher will exhaust all {@link RabbitMQConfiguration} instances when trying to publish a message and fails
 * only when every one of the
 * configurations fail to connect or fail otherwise.
 * This class implements the Spring {@link org.springframework.beans.factory.DisposableBean} and calls
 * {@link #closeConnections()} method to
 * cleanup connections when the application context is torn down. The dependence on Spring is justified by the need to
 * close connections cleanly
 * during application shutdown.
 * @author Jagadeesh Huliyar
 * @version 1.0, 26/06/2013
 */

public class LastUsedRabbitMQMessagePublisherImpl extends RabbitMQMessagePublisherImpl
{
	private int lastUsedConfigurationIndex = 0;
	
	public void publish(Object message) throws MessagingException 
	{
		validateMessage(message);
		try
		{
			publishToConnection(message,lastUsedConfigurationIndex);
		}
		catch(Exception e)
		{
			lastUsedConfigurationIndex = publishWithRoundRobinPolicy(message);
		}
	}


}