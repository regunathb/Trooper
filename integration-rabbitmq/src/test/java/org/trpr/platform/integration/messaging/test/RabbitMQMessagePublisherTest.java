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
public class RabbitMQMessagePublisherTest {

	/**
	 * Test method
	 */
	public static void main(String[] args) {
		final RabbitMQMessagePublisherImpl publisher = new RabbitMQMessagePublisherImpl();
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
		conf.setDurableMessageCommitCount(100);
		rabbitMQConfigurations.add(conf);
		publisher.setRabbitMQConfigurations(rabbitMQConfigurations);
		
		final RabbitMQMessageConsumerImpl consumer = new RabbitMQMessageConsumerImpl();
		consumer.setRabbitMQConfigurations(rabbitMQConfigurations);
		
		System.out.println("Queue depth from : " + consumer.getClass().getName() + " - " + consumer.getQueueDepth());
		
		/*
		while (true) {
			try {
				publisher.publishString("Hello");
				System.out.println("Published a message");
			} catch (MessagingException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		*/
		
		/*
		Thread[] threads = new Thread[10];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread() {
				public void run() {
					for (int j = 0; j < 10000; j++) {
						try {
							publisher.publish("Hello");
							//System.out.println(j + " : Published a message from thread : " + Thread.currentThread().getName());
						} catch (MessagingException e) {
							e.printStackTrace();
						}
					}
				}
			};
			threads[i].start();		
		}		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		*/
		
			
		int count = 0;
		while(count < 10) {
			System.out.println("Consuming : " + consumer.consumeString());
			count += 1;
		}
		
		
		// finally close connections
		//publisher.closeConnections();
		consumer.closeConnections();
		
	}
		
}
