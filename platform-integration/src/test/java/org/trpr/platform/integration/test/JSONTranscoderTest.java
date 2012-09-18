package org.trpr.platform.integration.test;


/**
 * Test class for org.trpr.platform.integration.impl.json.JSONTranscoderImpl 
 * 
 * @author Regunath B
 */
public class JSONTranscoderTest {

	/** Test method */
	public static void main(String[] args) {
		org.trpr.platform.model.event.PlatformEvent event = new org.trpr.platform.model.event.PlatformEvent();
		event.setEventMessage("Hi there");
		event.setCreatedDate(java.util.Calendar.getInstance());	
		org.trpr.platform.model.event.PlatformEventXML eventXML = new org.trpr.platform.model.event.PlatformEventXML();
		eventXML.setPlatformEvent(event);
		String json = new org.trpr.platform.integration.impl.json.JSONTranscoderImpl().marshal(eventXML);
		// edit the generated XML to introduce non-existent tags in the schema to test if unmarshaling can ignore extra elements in XML 
		System.out.println(json);
		org.trpr.platform.model.event.PlatformEventXML eventXML1 = (org.trpr.platform.model.event.PlatformEventXML)
				new org.trpr.platform.integration.impl.json.JSONTranscoderImpl().unmarshal(json,org.trpr.platform.model.event.PlatformEventXML.class);
		System.out.println(eventXML1.getPlatformEvent().getEventMessage());
		System.out.println(new org.trpr.platform.integration.impl.json.JSONTranscoderImpl().marshal(eventXML1));
	}
	
}
