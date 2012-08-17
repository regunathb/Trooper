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

package org.trpr.platform.servicefw.impl.event;

import org.trpr.platform.core.impl.event.EndpointEventProducerImpl;
import org.trpr.platform.servicefw.spi.event.ServiceEventProducer;

/**
 * The <code>ServiceEventProducerImpl</code> is a sub-type of {@link EndpointEventProducerImpl} used for publishing events to URI endpoints
 * defined in service container configurations. Also implements the {@link ServiceEventProducer} interface.
 * 
 * @author Regunath B
 * @version 1.0, 17/08/2012
 */
public class ServiceEventProducerImpl extends EndpointEventProducerImpl implements ServiceEventProducer {
}
