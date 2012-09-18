package org.trpr.example.service.request;

import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.example.model.service.greetingservice.GreetingServiceRequest;
import org.trpr.example.model.service.greetingservice.GreetingServiceRequestXML;

/**
 * Test class for testing service request serialization/de-serialization 
 * 
 * @author Regunath B
 */
public class ServiceRequestSerializer {

	/** Test method */
	public static void main(String[] args) {
		serializeToJSON();
	}
	
	private static void serializeToXML () {
		Earthling earthling = new Earthling();
		earthling.setFirstName("Regunath");
		earthling.setLastName("B");
		earthling.setDateOfBirth(java.util.Calendar.getInstance());		
		GreetingServiceRequest request = new GreetingServiceRequest();
		request.setEarthling(earthling);
		GreetingServiceRequestXML requestXML = new GreetingServiceRequestXML();
		requestXML.setGreetingServiceRequest(request);
		String xml = new org.trpr.platform.integration.impl.xml.XMLTranscoderImpl().marshal(requestXML);
		System.out.println(xml);
		System.out.println(((GreetingServiceRequestXML)
				new org.trpr.platform.integration.impl.xml.XMLTranscoderImpl().unmarshal(xml,GreetingServiceRequestXML.class)).getGreetingServiceRequest().getEarthling().getFirstName());
		
	}

	private static void serializeToJSON () {
		Earthling earthling = new Earthling();
		earthling.setFirstName("Regunath");
		earthling.setLastName("B");
		earthling.setDateOfBirth(java.util.Calendar.getInstance());		
		GreetingServiceRequest request = new GreetingServiceRequest();
		request.setEarthling(earthling);
		GreetingServiceRequestXML requestXML = new GreetingServiceRequestXML();
		requestXML.setGreetingServiceRequest(request);
		String json = new org.trpr.platform.integration.impl.json.JSONTranscoderImpl().marshal(requestXML);
		System.out.println(json);
		System.out.println(((GreetingServiceRequestXML)
				new org.trpr.platform.integration.impl.json.JSONTranscoderImpl().unmarshal(json,GreetingServiceRequestXML.class)).getGreetingServiceRequest().getEarthling().getFirstName());
		
	}

}
