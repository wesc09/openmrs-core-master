/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.openmrs.Order;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Allows for serializing/deserializing a Order object to a string so that Spring knows how to pass
 * a Order back and forth through an html form or other medium
 * <br>
 * In version 1.9, added ability for this to also retrieve objects by uuid
 * 
 * @see Order
 */
public class OrderEditor extends PropertyEditorSupport {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * @should set using id
	 * @should set using uuid
	 * 
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		OrderService ps = Context.getOrderService();
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getOrder(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				Order order = ps.getOrderByUuid(text);
				setValue(order);
				if (order == null) {
					log.error("Error setting text: " + text, ex);
					throw new IllegalArgumentException("Order not found: " + ex.getMessage());
				}
			}
		} else {
			setValue(null);
		}
	}
	
	/**
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	@Override
	public String getAsText() {
		Order t = (Order) getValue();
		if (t == null) {
			return "";
		} else {
			return t.getOrderId().toString();
		}
	}
	
}
