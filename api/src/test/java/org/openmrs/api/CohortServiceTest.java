/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Tests methods in the CohortService class TODO add all the rest of the tests
 */
public class CohortServiceTest extends BaseContextSensitiveTest {
	
	protected static final String CREATE_PATIENT_XML = "org/openmrs/api/include/PatientServiceTest-createPatient.xml";
	
	protected static final String COHORT_XML = "org/openmrs/api/include/CohortServiceTest-cohort.xml";

	protected static CohortService service = null;
	
	/**
	 * Run this before each unit test in this class. The "@Before" method in
	 * {@link BaseContextSensitiveTest} is run right before this method.
	 * 
	 * @throws Exception
	 */
	@Before
	public void runBeforeAllTests() {
		service = Context.getCohortService();
	}
	
	/**
	 * @see CohortService#getCohort(String)
	 */
	@Test
	public void getCohort_shouldOnlyGetNonVoidedCohortsByName() {
		executeDataSet(COHORT_XML);
		
		// make sure we have two cohorts with the same name and the first is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).getVoided());
		assertFalse(allCohorts.get(1).getVoided());
		
		// now do the actual test: getCohort by name and expect a non voided cohort
		Cohort exampleCohort = service.getCohort("Example Cohort");
		assertNotNull(exampleCohort);
		assertEquals(1, exampleCohort.size());
		assertFalse(exampleCohort.getVoided());
	}
	
	/**
	 * @see CohortService#getCohortByUuid(String)
	 */
	@Test
	public void getCohortByUuid_shouldFindObjectGivenValidUuid() {
		executeDataSet(COHORT_XML);
		String uuid = "h9a9m0i6-15e6-467c-9d4b-mbi7teu9lf0f";
		Cohort cohort = Context.getCohortService().getCohortByUuid(uuid);
		Assert.assertEquals(1, (int) cohort.getCohortId());
	}
	
	/**
	 * @see CohortService#getCohortByUuid(String)
	 */
	@Test
	public void getCohortByUuid_shouldReturnNullIfNoObjectFoundWithGivenUuid() {
		assertNull(Context.getCohortService().getCohortByUuid("some invalid uuid"));
	}
	
	/**
	 * @see CohortService#purgeCohort(Cohort)
	 */
	@Test
	public void purgeCohort_shouldDeleteCohortFromDatabase() {
		executeDataSet(COHORT_XML);
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertEquals(2, allCohorts.size());
		service.purgeCohort(allCohorts.get(0));
		allCohorts = service.getAllCohorts(true);
		assertEquals(1, allCohorts.size());
	}
	
	/**
	 * @see CohortService#getCohorts(String)
	 */
	@Test
	public void getCohorts_shouldMatchCohortsByPartialName() {
		executeDataSet(COHORT_XML);
		List<Cohort> matchedCohorts = service.getCohorts("Example");
		assertEquals(2, matchedCohorts.size());
		matchedCohorts = service.getCohorts("e Coh");
		assertEquals(2, matchedCohorts.size());
		matchedCohorts = service.getCohorts("hort");
		assertEquals(2, matchedCohorts.size());
		matchedCohorts = service.getCohorts("Examples");
		assertEquals(0, matchedCohorts.size());
	}
	
	/**
	 * @see CohortService#saveCohort(Cohort)
	 */
	@Test
	public void saveCohort_shouldCreateNewCohorts() {
		executeDataSet(COHORT_XML);
		
		// make sure we have two cohorts
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		
		// make and save a new one
		Integer[] ids = { 2, 3 };
		Cohort newCohort = new Cohort("a third cohort", "a  cohort to add for testing", ids);
		service.saveCohort(newCohort);
		
		// see if the new cohort shows up in the list of cohorts
		allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(3, allCohorts.size());
	}
	
	/**
	 * @see CohortService#saveCohort(Cohort)
	 */
	@Test
	public void saveCohort_shouldUpdateAnExistingCohort() {
		executeDataSet(COHORT_XML);
		
		// get and modify a cohort in the  data set
		String modifiedCohortDescription = "This description has been modified in a test";
		Cohort cohortToModify = service.getCohort(2);
		cohortToModify.setDescription(modifiedCohortDescription);
		
		// save the modified cohort back to the data set, see if the modification is there
		service.saveCohort(cohortToModify);
		assertTrue(service.getCohort(2).getDescription().equals(modifiedCohortDescription));
	}
	
	/**
	 * @see CohortService#voidCohort(Cohort,String)
	 */
	@Test
	public void voidCohort_shouldFailIfReasonIsEmpty() {
		executeDataSet(COHORT_XML);
		
		// Get a non-voided, valid Cohort and try to void it with a null reason
		Cohort exampleCohort = service.getCohort("Example Cohort");
		assertNotNull(exampleCohort);
		assertFalse(exampleCohort.getVoided());
		
		// Now get the Cohort and try to void it with an empty reason
		exampleCohort = service.getCohort("Example Cohort");
		assertNotNull(exampleCohort);
		assertFalse(exampleCohort.getVoided());
		
		try {
			service.voidCohort(exampleCohort, "");
			Assert.fail("voidCohort should fail with exception if reason is empty");
		}
		catch (Exception e) {}
	}
	
	/**
	 * @see CohortService#voidCohort(Cohort,String)
	 */
	@Test
	public void voidCohort_shouldFailIfReasonIsNull() {
		executeDataSet(COHORT_XML);
		
		// Get a non-voided, valid Cohort and try to void it with a null reason
		Cohort exampleCohort = service.getCohort("Example Cohort");
		assertNotNull(exampleCohort);
		assertFalse(exampleCohort.getVoided());
		
		try {
			service.voidCohort(exampleCohort, null);
			Assert.fail("voidCohort should fail with exception if reason is null.");
		}
		catch (Exception e) {}
		
		// Now get the Cohort and try to void it with an empty reason
		exampleCohort = service.getCohort("Example Cohort");
		assertNotNull(exampleCohort);
		assertFalse(exampleCohort.getVoided());
		
		try {
			service.voidCohort(exampleCohort, "");
			Assert.fail("voidCohort should fail with exception if reason is empty");
		}
		catch (Exception e) {}
	}
	
	/**
	 * @see CohortService#voidCohort(Cohort,String)
	 */
	@Test
	public void voidCohort_shouldNotChangeAnAlreadyVoidedCohort() {
		executeDataSet(COHORT_XML);
		
		// make sure we have an already voided cohort
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).getVoided());
		
		// Make sure the void reason is different from the reason to be given in the test
		assertNotNull(allCohorts.get(0).getVoidReason());
		String reasonAlreadyVoided = allCohorts.get(0).getVoidReason();
		String voidedForTest = "Voided for test";
		assertFalse(voidedForTest.equals(reasonAlreadyVoided));
		
		// Try to void and see if the void reason changes as a result
		Cohort voidedCohort = service.voidCohort(allCohorts.get(0), voidedForTest);
		assertFalse(voidedCohort.getVoidReason().equals(voidedForTest));
		assertTrue(voidedCohort.getVoidReason().equals(reasonAlreadyVoided));
		
	}
	
	/**
	 * @see CohortService#voidCohort(Cohort,String)
	 */
	@Test
	public void voidCohort_shouldVoidCohort() {
		executeDataSet(COHORT_XML);
		
		// make sure we have a cohort that is not voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertFalse(allCohorts.get(1).getVoided());
		
		service.voidCohort(allCohorts.get(1), "voided for Test");
		assertTrue(allCohorts.get(1).getVoided());
	}
	
	/**
	 * @see CohortService#getCohort(Integer)
	 */
	@Test
	public void getCohort_shouldGetCohortById() {
		executeDataSet(COHORT_XML);
		
		Cohort cohortToGet = service.getCohort(2);
		assertNotNull(cohortToGet);
		assertTrue(cohortToGet.getCohortId() == 2);
	}
	
	/**
	 * @see CohortService#getCohort(String)
	 */
	@Test
	public void getCohort_shouldGetCohortGivenAName() {
		executeDataSet(COHORT_XML);
		
		Cohort cohortToGet = service.getCohort("Example Cohort");
		assertTrue(cohortToGet.getCohortId() == 2);
	}
	
	/**
	 * @see CohortService#getCohort(String)
	 */
	@Test
	public void getCohort_shouldGetTheNonvoidedCohortIfTwoExistWithSameName() {
		executeDataSet(COHORT_XML);
		
		// check to see if both cohorts have the same name and if one is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(allCohorts.get(0).getName(), allCohorts.get(1).getName());
		assertTrue(allCohorts.get(0).getVoided());
		assertFalse(allCohorts.get(1).getVoided());
		// the non-voided cohort should have an id of 2
		assertTrue(allCohorts.get(1).getCohortId() == 2);
		
		// ask for the cohort by name
		Cohort cohortToGet = service.getCohort("Example Cohort");
		// see if the non-voided one got returned
		assertTrue(cohortToGet.getCohortId() == 2);
	}
	
	@Test
	public void getAllCohorts_shouldGetAllNonvoidedCohortsInDatabase() {
		executeDataSet(COHORT_XML);
		
		// call the method
		List<Cohort> allCohorts = service.getAllCohorts();
		assertNotNull(allCohorts);
		// there is only one non-voided cohort in the data set
		assertEquals(1, allCohorts.size());
		assertFalse(allCohorts.get(0).getVoided());
	}
	
	/**
	 * @see CohortService#getAllCohorts()
	 */
	@Test
	public void getAllCohorts_shouldNotReturnAnyVoidedCohorts() {
		executeDataSet(COHORT_XML);
		
		// make sure we have two cohorts, the first of which is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).getVoided());
		assertFalse(allCohorts.get(1).getVoided());
		
		// now call the target method and see if the voided cohort shows up
		allCohorts = service.getAllCohorts();
		assertNotNull(allCohorts);
		// only the non-voided cohort should be returned
		assertEquals(1, allCohorts.size());
		assertFalse(allCohorts.get(0).getVoided());
	}
	
	/**
	 * @see CohortService#getAllCohorts(null)
	 */
	@Test
	public void getAllCohorts_shouldReturnAllCohortsAndVoided() {
		executeDataSet(COHORT_XML);
		
		//data set should have two cohorts, one of which is voided
		List<Cohort> allCohorts = service.getAllCohorts(true);
		assertNotNull(allCohorts);
		assertEquals(2, allCohorts.size());
		assertTrue(allCohorts.get(0).getVoided());
		assertFalse(allCohorts.get(1).getVoided());
		
		// if called with false parameter, should not return the voided one
		allCohorts = service.getAllCohorts(false);
		assertNotNull(allCohorts);
		// only the non-voided cohort should be returned
		assertEquals(1, allCohorts.size());
		assertFalse(allCohorts.get(0).getVoided());
	}
	
	/**
	 * @see CohortService#getCohorts(String)
	 */
	@Test
	public void getCohorts_shouldNeverReturnNull() {
		executeDataSet(COHORT_XML);
		
		String invalidFragment = "Not Present";
		//data set should have two cohorts, one of which is voided
		List<Cohort> allCohorts = service.getCohorts(invalidFragment);
		assertNotNull(allCohorts);
	}
	
	/**
	 * @see CohortService#getCohortsContainingPatient(Patient)
	 */
	@Test
	public void getCohortsContainingPatient_shouldNotReturnVoidedCohorts() {
		executeDataSet(COHORT_XML);
		
		// make sure we have two cohorts, the first of which is voided
		assertTrue(service.getCohort(1).getVoided());
		assertFalse(service.getCohort(2).getVoided());
		
		// add a patient to both cohorts
		Patient patientToAdd = new Patient(7);
		service.addPatientToCohort(service.getCohort(1), patientToAdd);
		service.addPatientToCohort(service.getCohort(2), patientToAdd);
		assertTrue(service.getCohort(1).contains(patientToAdd.getPatientId()));
		assertTrue(service.getCohort(2).contains(patientToAdd.getPatientId()));

		// call the method and it should not return the voided cohort
		List<Cohort> cohortsWithPatientAdded = service.getCohortsContainingPatient(patientToAdd);
		assertNotNull(cohortsWithPatientAdded);
		assertFalse(cohortsWithPatientAdded.contains(service.getCohort(1)));
		
	}
	
	/**
	 * @see CohortService#getCohortsContainingPatient(Patient)
	 */
	@Test
	public void getCohortsContainingPatient_shouldReturnCohortsThatHaveGivenPatient() {
		executeDataSet(COHORT_XML);
		
		Patient patientToAdd = new Patient(7);
		service.addPatientToCohort(service.getCohort(2), patientToAdd);
		assertTrue(service.getCohort(2).contains(patientToAdd.getPatientId()));
		
		List<Cohort> cohortsWithGivenPatient = service.getCohortsContainingPatient(patientToAdd);
		assertTrue(cohortsWithGivenPatient.contains(service.getCohort(2)));
	}
	
	/**
	 * @see CohortService#addPatientToCohort(Cohort,Patient)
	 */
	@Test
	public void addPatientToCohort_shouldAddAPatientAndSaveTheCohort() {
		executeDataSet(COHORT_XML);
		
		// make a patient, add it using the method
		Patient patientToAdd = new Patient(4);
		service.addPatientToCohort(service.getCohort(2), patientToAdd);
		// proof of "save the cohort": see if the patient is in the cohort
		assertTrue(service.getCohort(2).contains(4));
	}
	
	/**
	 * @see CohortService#addPatientToCohort(Cohort,Patient)
	 */
	@Test
	public void addPatientToCohort_shouldNotFailIfCohortAlreadyContainsPatient() {
		executeDataSet(COHORT_XML);
		
		// make a patient, add it using the method
		Patient patientToAdd = new Patient(4);
		service.addPatientToCohort(service.getCohort(2), patientToAdd);
		assertTrue(service.getCohort(2).contains(4));
		
		// do it again to see if it fails
		try {
			service.addPatientToCohort(service.getCohort(2), patientToAdd);
		}
		catch (Exception e) {
			Assert.fail("addPatientToCohort(Cohort,Patient) fails when cohort already contains patient.");
		}
	}
	
	@Test
	public void removePatientFromCohort_shouldNotFailIfCohortDoesNotContainPatient() {
		executeDataSet(COHORT_XML);
		
		// make a patient
		Patient patientToAddThenRemove = new Patient(4);
		// verify that the patient is not already in the Cohort
		assertFalse(service.getCohort(2).contains(patientToAddThenRemove));
		// try to remove it from the cohort without failing
		try {
			service.removePatientFromCohort(service.getCohort(2), patientToAddThenRemove);
		}
		catch (Exception e) {
			Assert.fail("removePatientFromCohort(Cohort,Patient) should not fail if cohort doesn't contain patient");
		}
	}
	
	@Test
	public void removePatientFromCohort_shouldSaveCohortAfterRemovingPatient() {
		executeDataSet(COHORT_XML);
		
		// make a patient, add it using the method
		Patient patientToAddThenRemove = new Patient(4);
		service.addPatientToCohort(service.getCohort(2), patientToAddThenRemove);
		assertTrue(service.getCohort(2).contains(patientToAddThenRemove.getPatientId()));
		service.removePatientFromCohort(service.getCohort(2), patientToAddThenRemove);
		List<CohortMembership> memberList = service.getCohort(2)
				.getMembers().stream()
				.filter(m -> m.getPatient().getPatientId().equals(patientToAddThenRemove.getPatientId()))
				.collect(Collectors.toList());
		CohortMembership memberWithPatientToRemove = memberList.get(0);
		assertNotNull(memberWithPatientToRemove.getEndDate());
	}

	@Test
	public void addMembershipToCohort_shouldAddMembershipToCohort() {
		executeDataSet(COHORT_XML);
		
		Patient p = new Patient(4);
		CohortMembership memberToAdd = new CohortMembership(p);
		service.addMembershipToCohort(service.getCohort(1), memberToAdd);
		assertTrue(service.getCohort(1).contains(p));
	}

	@Test
	public void removeMembershipFromCohort_shouldRemoveMembershipFromCohort() {
		executeDataSet(COHORT_XML);

		CohortMembership memberToAddThenRemove = new CohortMembership(new Patient(4));
		service.addMembershipToCohort(service.getCohort(1), memberToAddThenRemove);
		assertTrue(service.getCohort(1).contains(memberToAddThenRemove.getPatient()));
		assertNull(memberToAddThenRemove.getEndDate());

		service.removeMemberShipFromCohort(service.getCohort(1), memberToAddThenRemove);
		assertNotNull(memberToAddThenRemove.getEndDate());
	}

	@Test
	public void patientVoided_shouldVoidMemberships() {
		executeDataSet(COHORT_XML);

		Cohort cohort = Context.getCohortService().getCohort(2);
		Patient voidedPatient = new Patient(7);
		voidedPatient.setVoided(true);
		voidedPatient.setDateVoided(new Date());
		voidedPatient.setVoidedBy(Context.getAuthenticatedUser());
		voidedPatient.setVoidReason("Voided as a result of the associated patient getting voided");

		CohortMembership newMemberContainingVoidedPatient = new CohortMembership(voidedPatient);
		cohort.addMembership(newMemberContainingVoidedPatient);
		assertTrue(cohort.contains(voidedPatient));

		service.patientVoided(voidedPatient);
		assertTrue(newMemberContainingVoidedPatient.getVoided());
		assertEquals(voidedPatient.getDateVoided(), newMemberContainingVoidedPatient.getDateVoided());
		assertEquals(voidedPatient.getVoidedBy(), newMemberContainingVoidedPatient.getVoidedBy());
		assertEquals(voidedPatient.getVoidReason(), newMemberContainingVoidedPatient.getVoidReason());
	}
	
	@Test
	public void patientUnvoided_shouldUnvoidMemberships() {
		executeDataSet(COHORT_XML);
		
		Cohort cohort = Context.getCohortService().getCohort(2);
		Patient unvoidedPatient = new Patient(7);
		User voidedBy = Context.getAuthenticatedUser();
		Date dateVoided = new Date();
		String voidReason = "Associated patient is voided";
		
		CohortMembership voidedMembership = new CohortMembership(unvoidedPatient);
		voidedMembership.setVoided(true);
		voidedMembership.setVoidedBy(voidedBy);
		voidedMembership.setDateVoided(dateVoided);
		voidedMembership.setVoidReason(voidReason);
		
		cohort.addMembership(voidedMembership);
		service.patientUnvoided(unvoidedPatient, voidedBy, dateVoided, voidReason);
		
		assertFalse(voidedMembership.getVoided());
		assertNull(voidedMembership.getVoidedBy());
		assertNull(voidedMembership.getDateVoided());
		assertNull(voidedMembership.getVoidReason());
	}
	
	@Test
	public void getMemberships_shouldGetMembershipsAsOfADate() throws ParseException {
		executeDataSet(COHORT_XML);

		Cohort cohort = Context.getCohortService().getCohort(1);

		CohortMembership newMember = new CohortMembership((new Patient(4)));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateToTest = dateFormat.parse("2016-11-01 00:00:00");
		newMember.setStartDate(dateToTest);
		service.addMembershipToCohort(cohort, newMember);

		List<CohortMembership> membersAsOfDate = cohort.getMemberships(dateToTest);
		assertFalse(membersAsOfDate.isEmpty());
		assertTrue(membersAsOfDate.stream().anyMatch(m -> m.getStartDate().equals(dateToTest)));
	}
}
