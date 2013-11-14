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

import java.io.IOException;

import org.mule.api.MuleContext;
import org.mule.config.ConfigResource;
import org.mule.config.spring.MissingParserProblemReporter;
import org.mule.config.spring.MuleContextPostProcessor;
import org.mule.util.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * The <code>MuleApplicationContext</code> class is a code-port of the original Mule {@link org.mule.config.spring.MuleApplicationContext} that works with 
 * Spring 3.1.x releases and later versions.
 * This implementation also performs the same tasks as defined by the Mule implementation (description copied from original source):
 * 
 * <code>MuleApplicationContext</code> is a simple extension application context that allows resources to be loaded from the Classpath of file system using the
 * MuleBeanDefinitionReader.
 * 
 * However, this implementation uses a custom {@link MuleBeanDefinitionDocumentReader} that can work with Spring 3.1.x releases and later versions. 
 * 
 * @author Regunath B
 * @version 1.0, 14/11/2013
 */

public class MuleApplicationContext extends AbstractXmlApplicationContext
{
    private MuleContext muleContext;
    private Resource[] springResources;
    private static final ThreadLocal<MuleContext> currentMuleContext = new ThreadLocal<MuleContext>();
    /**
     * Parses configuration files creating a spring ApplicationContext which is used
     * as a parent registry using the SpringRegistry registry implementation to wraps
     * the spring ApplicationContext
     * 
     * @param configResources
     * @see org.mule.config.spring.SpringRegistry
     */
    public MuleApplicationContext(MuleContext muleContext, ConfigResource[] configResources)
            throws BeansException
    {
        this(muleContext, convert(configResources));
    }

    public MuleApplicationContext(MuleContext muleContext, Resource[] springResources) throws BeansException
    {
        this.muleContext = muleContext;
        this.springResources = springResources;
    }

    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) 
    {
        super.prepareBeanFactory(beanFactory);
        beanFactory.addBeanPostProcessor(new MuleContextPostProcessor(muleContext));
    }

    private static Resource[] convert(ConfigResource[] resources)
    {
        Resource[] configResources = new Resource[resources.length];
        for (int i = 0; i < resources.length; i++)
        {
            ConfigResource resource = resources[i];
            if(resource.getUrl()!=null)
            {
                configResources[i] = new UrlResource(resource.getUrl());
            }
            else
            {
                try
                {
                    configResources[i] = new ByteArrayResource(IOUtils.toByteArray(resource.getInputStream()), resource.getResourceName());
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        return configResources;
    }

    @Override
    protected Resource[] getConfigResources()
    {
        return springResources;
    }

    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException
    {
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        //hook in our custom hierarchical reader
        beanDefinitionReader.setDocumentReaderClass(MuleBeanDefinitionDocumentReader.class); // use the Trooper MuleBeanDefinitionDocumentReader instead of the original one from Mule
        //add error reporting
        beanDefinitionReader.setProblemReporter(new MissingParserProblemReporter());

        // Communicate mule context to parsers
        try
        {
            currentMuleContext.set(muleContext);
            beanDefinitionReader.loadBeanDefinitions(springResources);
        }
        finally
        {
            currentMuleContext.remove();
        }
    }

    @Override
    protected DefaultListableBeanFactory createBeanFactory()
    {
        //Copy all postProcessors defined in the defaultMuleConfig so that they get applied to the child container
        DefaultListableBeanFactory bf = super.createBeanFactory();
        if (getParent() != null)
        {
            //Copy over all processors
            AbstractBeanFactory beanFactory = (AbstractBeanFactory)getParent().getAutowireCapableBeanFactory();
            bf.copyConfigurationFrom(beanFactory);
        }
        return bf;
    }

    public MuleContext getMuleContext()
    {
        return muleContext;
    }

    public static ThreadLocal<MuleContext> getCurrentMuleContext()
    {
        return currentMuleContext;
    }
}
