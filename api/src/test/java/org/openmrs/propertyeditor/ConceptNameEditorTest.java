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

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.test.BaseContextSensitiveTest;

public class ConceptNameEditorTest extends BaseContextSensitiveTest {
	
	/**
	 * @see ConceptNameEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldSetUsingId() {
		ConceptNameEditor editor = new ConceptNameEditor();
		editor.setAsText("1439");
		Assert.assertNotNull(editor.getValue());
	}
	
	/**
	 * @see ConceptNameEditor#setAsText(String)
	 */
	@Test
	public void setAsText_shouldSetUsingUuid() {
		ConceptNameEditor editor = new ConceptNameEditor();
		editor.setAsText("9bc5693a-f558-40c9-8177-145a4b119ca7");
		Assert.assertNotNull(editor.getValue());
	}
}
