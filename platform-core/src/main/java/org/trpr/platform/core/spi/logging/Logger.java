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

package org.trpr.platform.core.spi.logging;

/**
 * The <code>Logger</code> interface is a sub-type of the SLF4j logging framework's Logger {@linkplain http://www.slf4j.org/} .
 * The Trooper logging framework is expected to evolve to support log entry correlation, distributed logging and maybe even a 
 * centralized service where relevant.
 * 
 *  This interface abstracts present and future implementations in a manner transparent to clients of the logging framework.
 *  A Logger implementation may be instantiated using a suitable Factory class - either container based such as Spring or using factory methods 
 *  in a Java class.
 *
 * <p>Configuration of the underlying logging system is implementation specific.
 *
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */
public interface Logger extends org.slf4j.Logger {

}
