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

package org.trpr.platform.core.impl.management.jmx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.management.Notification;

import org.trpr.platform.core.spi.management.jmx.AppInstanceAwareMBean;
import org.trpr.platform.model.event.PlatformEvent;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedNotification;
import org.springframework.jmx.export.annotation.ManagedNotifications;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.notification.NotificationPublisher;
import org.springframework.jmx.export.notification.NotificationPublisherAware;

/**
 * Class <code>JMXNotificationDispatcher</code> class is an implementation of {@link org.springframework.jmx.export.notification.NotificationPublisherAware} 
 * interface that may be used to publish {@link PlatformEvent} data to the JMX system.
 * 
 * @author Regunath B
 * @version 1.0, 17/05/2012
 */
@ManagedResource(objectName = "spring.application:type=Trooper,application=JMX-Notification-Dispatcher,name=Default-Dispatcher", description = "Trooper JMX Notification Dispatcher")
@ManagedNotifications(value=@ManagedNotification(name="TrprNotifications", description="Trooper Notifications", notificationTypes={"javax.management.Notification"}))
public class JMXNotificationDispatcher extends AppInstanceAwareMBean implements NotificationPublisherAware {

	/**Count of notifications dispatched */
	private long eventNotificationCount;
	private long exceptionNotificationCount;
	
	/**
	 * The NotificationPublisher instance to use for JMX notifications
	 */
	private NotificationPublisher publisher;
	
	/**
	 * Interface method implementation.
	 * 
	 * @see org.springframework.jmx.export.notification.NotificationPublisherAware#setNotificationPublisher(org.springframework.jmx.export.notification.NotificationPublisher)
	 */
	public void setNotificationPublisher(NotificationPublisher publisher) {
		this.publisher = publisher;
	}
		
	/**
	 * Sends the specified event details to an MBean Server by constructing a {@link javax.management.Notification} instance using the specified
	 * event data.
	 * @event the PlatformEvent whose data contents are sent to the MBean server
	 */
	public void dispatchPlatformEvent(PlatformEvent event) {
		String message = event.getEventMessage();
		final Notification notification=new Notification(event.getClass().getName(), event.getEventSource(), 
				event.getEntityIdentifier().getUniqueId(), event.getCreatedDate().getTimeInMillis(), getHostIP() + message);
		this.publisher.sendNotification(notification);
		this.eventNotificationCount++;
	}
	
	/**
	 * Convenience method to dispatch Application and System exception details to the JMX sub-system
	 * @param exception the Exception details to dispatch to JMX
	 */
	public void dispatchException(Throwable exception, String source) {
		if (exception != null) {
			StringWriter writer = new StringWriter();
			exception.printStackTrace(new PrintWriter(writer));
			final Notification notification=new Notification(exception.getClass().getName(), source, 
					exception.hashCode(), System.currentTimeMillis(), getHostIP() + exception.getMessage() + "\n" + writer.toString());
			this.publisher.sendNotification(notification);
			this.exceptionNotificationCount++;
		}
	}

	/** Managed attributes accessor methods*/
	@ManagedAttribute
	public long getEventNotificationCount() {
		return eventNotificationCount;
	}
	
	@ManagedAttribute
	public long getExceptionNotificationCount() {
		return exceptionNotificationCount;
	}
	
	/** 
	 * Helper method to return the local host IP address, enclosed in "[" "]", to be appended to messages.
	 * @return the local host's IP address or the String "UnknownHost" in case of errors
	 */
	private String getHostIP() {
		try {
			return "[" + InetAddress.getLocalHost().getHostAddress() + "] ";
		} catch (UnknownHostException uhe) {
			// do nothing.
		}
		return "[UnknownHost]";
	}

}

