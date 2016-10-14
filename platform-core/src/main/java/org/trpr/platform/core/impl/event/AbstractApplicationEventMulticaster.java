/*
 * Copyright 2012-2016, the original author or authors.
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

package org.trpr.platform.core.impl.event;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Abstract implementation of the {@link ApplicationEventMulticaster} interface,
 * providing the basic listener registration facility. This is thread unsafe version of
 * Spring's {@link org.springframework.context.event.AbstractApplicationEventMulticaster}
 * as Spring's version has global locks in critical flows like {@link #getApplicationListeners}.
 * <p/>
 * <p>Doesn't permit multiple instances of the same listener by default,
 * as it keeps listeners in a linked Set.
 * <p/>
 * <p>Implementing ApplicationEventMulticaster's actual {@link #multicastEvent} method
 * is left to subclasses. {@link PlatformEventMulticaster} simply multicasts
 * all events to all registered listeners, invoking them in the calling thread.
 * <p/>
 * @author Mohan Kumar Pandian
 * @version 2.0.0, 14/10/2016
 */
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster {
    private final Set<ApplicationListener<?>> applicationListeners;

    public AbstractApplicationEventMulticaster() {
        this.applicationListeners = new LinkedHashSet<>();
    }

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        this.applicationListeners.add(listener);
    }

    @Override
    public void addApplicationListenerBean(String listenerBeanName) {
        // No support for listener bean by name
    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        this.applicationListeners.remove(listener);
    }

    @Override
    public void removeApplicationListenerBean(String listenerBeanName) {
        // No support for listener bean by name
    }

    @Override
    public void removeAllListeners() {
        this.applicationListeners.clear();
    }

    /**
     * Thread unsafe version of spring's AbstractApplicationEventMulticaster
     *
     * @return Registered application listeners (and not beans)
     * @see org.springframework.context.event.AbstractApplicationEventMulticaster#getApplicationListeners()
     */
    protected Collection<ApplicationListener<?>> getApplicationListeners() {
        return applicationListeners;
    }
}
