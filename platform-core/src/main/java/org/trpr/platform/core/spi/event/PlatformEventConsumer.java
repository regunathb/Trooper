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

package org.trpr.platform.core.spi.event;

import org.springframework.context.ApplicationListener;

/**
 * Interface <code>PlatformEventConsumer</code> is a simple marker consumer interface for processing events. It extends the Spring
 * {@link ApplicationListener} interface. All event consumers must implement this interface.
 * 
 * @see PlatformEventProducer
 * 
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */

public interface PlatformEventConsumer extends ApplicationListener {
}
