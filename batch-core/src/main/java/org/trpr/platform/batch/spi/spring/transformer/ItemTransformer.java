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
package org.trpr.platform.batch.spi.spring.transformer;

import org.springframework.batch.item.ItemProcessor;

/**
 * The <code>ItemTransformer</code> extends the Spring Batch {@link ItemProcessor} and is used for transforming or filtering out data items passed to it.
 * As a transformer, this processor converts the input data into another type. As a filter, this processor returns <code>null</code> when {@link #process(Object) 
 * is invoked.
 * 
 * @author Regunath B
 * @version 1.0, 28 Aug 2012
 */
public interface ItemTransformer<I,O> extends ItemProcessor<I,O> {

}
