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
package org.trpr.platform.integration.spi.xml;

import javax.xml.transform.Result;

import org.trpr.platform.integration.spi.marshalling.Marshaller;

/**
 * The <code> XMLTranscoder </code> interface defines methods for Java-XML marshalling/unmarshalling.
 * 
 * @author Regunath B
 * @version 1.0, 25/05/2012
 */
public interface XMLTranscoder extends Marshaller {

	/**
	 * Converts specified Java Object to an equivalent output as determined by the specified {@link Result} implementation.
	 * The Result implementation may perform such activities as Transformation to produce the desired output. Possible transformations
	 * may include and not limited to 
	 * <pre>
	 * 	Ignoring Namespaces - using a StAXResult
	 *  Transforming output - using XSLT via JAXBResult 
	 * <pre> 
	 * @param object the Java Object to be marshalled to a suitable output
	 * @param result the Result implementation to write output as marshalling happens
	 * @throws XMLDataException  in case of errors during marshalling
	 */
	public void marshal(Object object, Result result) throws XMLDataException;
	
}
