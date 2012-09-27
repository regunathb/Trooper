package org.trpr.platform.integration.messaging.test;

import java.util.List;

import org.trpr.platform.integration.impl.messaging.RabbitMQConfiguration;
import org.trpr.platform.integration.impl.messaging.RabbitMQMessageConsumerImpl;
import org.trpr.platform.integration.impl.messaging.RabbitMQMessagePublisherImpl;
import org.trpr.platform.integration.spi.messaging.MessagingException;

/**
 * Test class for org.trpr.platform.integration.messaging.rabbitmq.RabbitMQMessagePublisherImpl 
 * 
 * @author Regunath B
 */
public class RabbitMQMessagePublisherTest2 {

	/**
	 * Test method
	 */
	public static void main(String[] args) {

		List<RabbitMQConfiguration> rabbitMQConfigurations = new java.util.LinkedList<RabbitMQConfiguration>();
		RabbitMQConfiguration conf = new RabbitMQConfiguration();
		conf.setHostName("localhost");
		conf.setVirtualHost("/");
		conf.setExchangeName("enrol");
		conf.setExchangeType("direct");
		conf.setDurable(true);
		conf.setPortNumber(5672);
		conf.setUserName("guest");
		conf.setPassword("guest");
		conf.setQueueName("helloInput");
		conf.setRoutingKey("helloInput");
		rabbitMQConfigurations.add(conf);


		final RabbitMQMessagePublisherImpl publisher = new RabbitMQMessagePublisherImpl();
		publisher.setRabbitMQConfigurations(rabbitMQConfigurations);

		final RabbitMQMessageConsumerImpl consumer = new RabbitMQMessageConsumerImpl();
		consumer.setRabbitMQConfigurations(rabbitMQConfigurations);

		//Consume any messages that are already in the queue
		while(consumer.getQueueDepth() > 1) {
			System.out.println(consumer.getQueueDepth() + " messages still in queue");
			System.out.println("Consuming : " + consumer.consumeString());
		}

		// Publish and consume 100 messages
		for (int i = 0; i < 10; i++) {
			publisher.publishString("Message # " + i);
			System.out.println("Consuming : " + consumer.consumeString());
		}

		// finally close connections
		publisher.closeConnections();
		consumer.closeConnections();

	}

}
