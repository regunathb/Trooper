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

package org.trpr.platform.spi.validation;

import org.trpr.platform.model.common.BusinessEntity;

/**
 * The <code>BusinessEntityValidationStrategy</code> interface exposes methods to be
 * implemented by validators of complex business entities.
 * 
 * @see BusinessEntity
 * 
 * @author Regunath B
 * @version 1.0, 24/05/2012
 */

public interface BusinessEntityValidationStrategy {

	/** Constants for signaling continuation of or vetoing the validation */
	public static final int CONTINUE_VALIDATION = 0;
	public static final int VETO_VALIDATION = 100;

	/**
	 * Validates the specified BusinessEntity and returns the result as a
	 * ValidationSummary. Implementations may not return null in case the
	 * specified entity is valid and must return a ValidationSummary instead
	 * that may have no results.
	 * 
	 * @param entity the BusinessEntity to be validated
	 * @return validation results as a ValidationSummary
	 */
	public ValidationSummary validate(BusinessEntity entity);

}
