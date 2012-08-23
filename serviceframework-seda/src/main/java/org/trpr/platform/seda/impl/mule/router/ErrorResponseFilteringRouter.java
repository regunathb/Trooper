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
package org.trpr.platform.seda.impl.mule.router;

import org.mule.api.MessagingException;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.MuleSession;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.routing.CouldNotRouteOutboundMessageException;
import org.mule.routing.outbound.AbstractOutboundRouter;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.servicefw.common.ServiceFrameworkConstants;
import org.trpr.platform.servicefw.impl.ServiceResponseImpl;
import org.trpr.platform.servicefw.spi.ServiceResponse;

/**
 * The <code>ErrorResponseFilteringRouter</code> class is a sub-type of the Mule AbstractOutboundRouter that filters out service responses that 
 * return error status in the response. 
 * 
 * @author Regunath B
 * @version 1.0, 23/08/2012
 */

public class ErrorResponseFilteringRouter<T> extends AbstractOutboundRouter {

	/**
	 * The Logger instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(ErrorResponseFilteringRouter.class);
	
	/**
	 * Overriden superclass method. Returns true only if the message payload is of type {@link ServiceResponseImpl}
	 * @see org.mule.api.routing.OutboundRouter#isMatch(org.mule.api.MuleMessage)
	 */
	public boolean isMatch(MuleMessage message) throws MessagingException {
		return (message.getPayload() instanceof ServiceResponse);
	}

	/**
	 * Overriden superclass method. Routes messages only if the method {@link ServiceResponseImpl#getStatusCode()} returns a SUCCESS. Else, simply
	 * logs the fact that the message is being consumed. Error responses (formatted or verbatim) are logged to a suitable sink using the Event framework.
	 * @see org.mule.api.routing.OutboundRouter#route(org.mule.api.MuleMessage, org.mule.api.MuleSession)
	 */
	@SuppressWarnings("unchecked")
	public MuleMessage route(MuleMessage message, MuleSession session) throws MessagingException {
		MuleMessage result = null;
		OutboundEndpoint endPoint = (OutboundEndpoint)getEndpoints().get(0);
		ServiceResponseImpl<T> serviceResponse = (ServiceResponseImpl<T>)message.getPayload();
		try {
			if (serviceResponse.getStatusCode().equalsIgnoreCase(String.valueOf(ServiceFrameworkConstants.SUCCESS_STATUS_CODE))) {
				if (endPoint.isSynchronous()) {
					result = send(session, message, endPoint);
				} else {
					dispatch(session, message, endPoint);
		            return null;
				}
			} else {
				// just log the fact that the router is not forwarding the message
				LOGGER.debug("Error response not being forwarded through for service response of type : " + serviceResponse.getResponseData().getClass().getName());
			}
	    } catch (MuleException e) {
	        throw new CouldNotRouteOutboundMessageException(message, endPoint, e);
	    }
	    return result;
	 }

}
