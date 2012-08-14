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

package org.trpr.platform.servicefw.spi.event;

import org.trpr.platform.core.spi.event.EndpointEventProducer;

/**
 * The <code>ServiceEventProducer</code> is a sub-type of EndpointEventProducer used primarily to publish events from services.
 * 
 * @author Regunath B
 * @version 1.0, 13/08/2012
 */
public interface ServiceEventProducer extends EndpointEventProducer {

}
