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
 * 
 * Copyright of the original source code, from which this implementation is derived
 * 
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 * 
 */

package org.trpr.platform.seda.impl.mule.spring;

import org.mule.api.MuleContext;
import org.mule.api.config.ConfigurationException;
import org.mule.api.lifecycle.LifecycleManager;
import org.mule.api.lifecycle.Startable;
import org.mule.api.registry.Registry;
import org.mule.config.ConfigResource;
import org.mule.config.builders.AbstractResourceConfigurationBuilder;
import org.mule.config.i18n.MessageFactory;
import org.mule.config.spring.SpringRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The <code>SpringXmlConfigurationBuilder</code> class is a code-port of the original Mule {@link org.mule.config.spring.SpringXmlConfigurationBuilder} that works with 
 * Spring 3.1.x releases and later versions.
 * This implementation also performs the same tasks as defined by the Mule implementation (description copied from original source):
 * 
 * <code>SpringXmlConfigurationBuilder</code> enables Mule to be configured from a Spring XML Configuration file used with Mule name-spaces. Multiple configuration
 * files can be loaded from this builder (specified as a comma-separated list).
 * 
 * However, this implementation uses a custom {@link MuleApplicationContext} that can work with Spring 3.1.x releases and later versions. 
 * 
 * @author Regunath B
 * @version 1.0, 14/11/2013
 */
public class SpringXmlConfigurationBuilder extends AbstractResourceConfigurationBuilder
{
    public static final String MULE_DEFAULTS_CONFIG = "default-mule-config.xml";

    /** Prepend "default-mule-config.xml" to the list of config resources. */
    private boolean useDefaultConfigResource = true;

    private Registry registry;
    
    private ApplicationContext parentContext;
    
    public SpringXmlConfigurationBuilder(String[] configResources) throws ConfigurationException
    {
        super(configResources);
    }

    public SpringXmlConfigurationBuilder(String configResources) throws ConfigurationException
    {
        super(configResources);
    }

    public SpringXmlConfigurationBuilder(ConfigResource[] configResources)
    {
        super(configResources);
    }

    protected void doConfigure(MuleContext muleContext) throws Exception
    {
        ConfigResource[] allResources;
        if (useDefaultConfigResource)
        {
            allResources = new ConfigResource[configResources.length + 1];
            allResources[0] = new ConfigResource(MULE_DEFAULTS_CONFIG);
            System.arraycopy(configResources, 0, allResources, 1, configResources.length);
        }
        else
        {
            allResources = configResources;
        }
        createSpringRegistry(muleContext, createApplicationContext(muleContext, allResources));
    }

    protected ApplicationContext createApplicationContext(MuleContext muleContext, ConfigResource[] configResources) throws Exception
    {
        return new MuleApplicationContext(muleContext, configResources); // use the Trooper MuleApplicationContext instead of the original one from Mule
    }
    
    protected void createSpringRegistry(MuleContext muleContext, ApplicationContext applicationContext) throws Exception
    {
        if (parentContext != null)
        {
            if (applicationContext instanceof ConfigurableApplicationContext)
            {
                registry = new SpringRegistry((ConfigurableApplicationContext) applicationContext, parentContext);
            }
            else
            {
                throw new ConfigurationException(MessageFactory.createStaticMessage("Cannot set a parent context if the ApplicationContext does not implement ConfigurableApplicationContext"));
            }
        }
        else
        {
            registry = new SpringRegistry(applicationContext);
        }

        // Note: The SpringRegistry must be created before applicationContext.refresh() gets called because
        // some beans may try to look up other beans via the Registry during preInstantiateSingletons().
        muleContext.addRegistry(1, registry);
        registry.initialise();
    }
    
    protected void applyLifecycle(LifecycleManager lifecycleManager) throws Exception
    {
        // If the MuleContext is started, start all objects in the new Registry.
        if (lifecycleManager.isPhaseComplete(Startable.PHASE_NAME))
        {
            lifecycleManager.applyPhase(registry.lookupObjects(Object.class), Startable.PHASE_NAME);
        }
    }
    
    public boolean isUseDefaultConfigResource()
    {
        return useDefaultConfigResource;
    }

    public void setUseDefaultConfigResource(boolean useDefaultConfigResource)
    {
        this.useDefaultConfigResource = useDefaultConfigResource;
    }

    protected ApplicationContext getParentContext()
    {
        return parentContext;
    }
    
    public void setParentContext(ApplicationContext parentContext)
    {
        this.parentContext = parentContext;
    }
}
