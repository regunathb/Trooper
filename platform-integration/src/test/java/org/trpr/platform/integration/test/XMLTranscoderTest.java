package org.trpr.platform.integration.test;

/**
 * Test class for org.trpr.platform.integration.impl.xml.XMLTranscoderImpl 
 * 
 * @author Regunath B
 */
public class XMLTranscoderTest {

	/** Test method */
	public static void main(String[] args) {
		org.trpr.platform.model.event.PlatformEvent event = new org.trpr.platform.model.event.PlatformEvent();
		event.setEventMessage("Hi there");
		event.setCreatedDate(java.util.Calendar.getInstance());		
		String xml = new org.trpr.platform.integration.impl.xml.XMLTranscoderImpl().marshal(event);
		// edit the generated XML to introduce non-existent tags in the schema to test if unmarshaling can ignore extra elements in XML 
		xml = xml.replace("</ns2:createdDate>", "</ns2:createdDate>\n<ns2:test>test text</ns2:test>");
		System.out.println(xml);
		System.out.println(((org.trpr.platform.model.event.PlatformEvent)
				new org.trpr.platform.integration.impl.xml.XMLTranscoderImpl().unmarshal(xml,org.trpr.platform.model.event.PlatformEvent.class)).getEventMessage());
	}
	
}
