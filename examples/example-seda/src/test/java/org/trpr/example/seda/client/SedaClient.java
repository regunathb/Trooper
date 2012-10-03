package org.trpr.example.seda.client;

import java.util.Calendar;
import java.util.List;

import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.platform.integration.impl.messaging.RabbitMQConfiguration;
import org.trpr.platform.integration.impl.messaging.RabbitMQMessagePublisherImpl;

/**
 * Test class for Trooper Orchestration profile samples 
 * 
 * @author Regunath B
 */
public class SedaClient {

	/**
	 * Test method
	 */
	public static void main(String[] args) {
		final RabbitMQMessagePublisherImpl publisher = new RabbitMQMessagePublisherImpl();
		List<RabbitMQConfiguration> rabbitMQConfigurations = new java.util.LinkedList<RabbitMQConfiguration>();
		RabbitMQConfiguration conf = new RabbitMQConfiguration();
		conf.setHostName("localhost");
		conf.setVirtualHost("/");
		conf.setExchangeName("myexchange");
		conf.setExchangeType("direct");
		conf.setPortNumber(5672);
		conf.setUserName("guest");
		conf.setPassword("guest");
		conf.setQueueName("greetingInput");
		conf.setRoutingKey("greetingInput");
		rabbitMQConfigurations.add(conf);
		publisher.setRabbitMQConfigurations(rabbitMQConfigurations);
		/*
		int count = 0;
		for (int j = 0; j < 10; j++) {
			Earthling e = new Earthling();
			e.setFirstName("Mr");
			e.setLastName("Trooper" + count + j);
			Calendar c  = Calendar.getInstance();
			c.set(Calendar.YEAR, 2010);
			e.setDateOfBirth(c);	
			publisher.publish(e);
			System.out.println(j + " : Published a message from thread : " + Thread.currentThread().getName());
			count += 1;
		}
		publisher.closeConnections();
		*/
		Thread[] threads = new Thread[1];
		for (int i = 0; i < threads.length; i++) {
			final int count = i;
			threads[i] = new Thread() {
				public void run() {
					for (int j = 0; j < 11; j++) {
						Earthling e = new Earthling();
						e.setFirstName("Mr");
						e.setLastName("Trooper" + count + j);
						Calendar c  = Calendar.getInstance();
						c.set(1974, 10, 16);
						e.setDateOfBirth(c);	
						publisher.publish(e);
						System.out.println(j + " : Published a message from thread : " + Thread.currentThread().getName());
					}
				}
			};
			threads[i].start();	
		}
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
		publisher.closeConnections();
		
	}
		
}
