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

import org.mule.config.spring.MuleHierarchicalBeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * The <code>MuleBeanDefinitionDocumentReader</code> class is an as-is code-port of the original Mule {@link org.mule.config.spring.MuleBeanDefinitionDocumentReader}
 * that is found in the Mule 3.x codebase:
 * 
 * groupId:org.mule.modules
 * artifactId:mule-module-spring-config
 * version:3.4.0
 * 
 * This class works with Spring 3.1.x releases and later versions. Description copied from original source:
 * 
 * Allows us to hook in our own Hierarchical Parser delegate. this enables the parsing of custom spring bean elements nested within each other
 * 
 * @author Regunath B
 * @version 1.0, 14/11/2013
 */

public class MuleBeanDefinitionDocumentReader extends DefaultBeanDefinitionDocumentReader
{

    @Override
    protected BeanDefinitionParserDelegate createDelegate(XmlReaderContext readerContext, Element root, BeanDefinitionParserDelegate parentDelegate)
    {
        BeanDefinitionParserDelegate delegate = new MuleHierarchicalBeanDefinitionParserDelegate(readerContext, this);
        delegate.initDefaults(root, parentDelegate);
        return delegate;
    }

    /* Keep backward compatibility with spring 3.0 */
    protected BeanDefinitionParserDelegate createHelper(XmlReaderContext readerContext, Element root)
    {
        BeanDefinitionParserDelegate delegate = new MuleHierarchicalBeanDefinitionParserDelegate(readerContext, this);
        delegate.initDefaults(root);
        return delegate;
    }

    /**
     * Override to reject configuration files with no namespace, e.g. mule legacy
     * configuration file.
     */
    @Override
    protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate)
    {
        if (!StringUtils.hasLength(root.getNamespaceURI()))
        {
            getReaderContext().error("Unable to locate NamespaceHandler for namespace [null]", root);
        }
        else
        {
            super.parseBeanDefinitions(root, delegate);
        }
    }

}
