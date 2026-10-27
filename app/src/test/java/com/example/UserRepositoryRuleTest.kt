package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.repository.UserRepository
import com.example.model.UserProfileDocument
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun saveUserProfile_authenticatedUser_registersProfileWithSATLimited() = runBlocking {
    val uid = signInTestUser("sat_user_1@satlimited.com")
    val repository = UserRepository(firestore)

    val profile = UserProfileDocument(
      userId = uid,
      name = "Simba Tembo",
      email = "sat_user_1@satlimited.com",
      organization = "SAT Limited",
      username = "@simba_sat",
      status = "Building Kora with SAT Limited 🌿",
      phone = "+263 77 123 4567"
    )

    val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveUserProfile(profile)
    }
    assertTrue(saveResult.isSuccess)

    val fetchResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.getUserProfile(uid)
    }
    assertTrue(fetchResult.isSuccess)
    val fetched = fetchResult.getOrThrow()
    assertNotNull(fetched)
    assertEquals(uid, fetched?.userId)
    assertEquals("SAT Limited", fetched?.organization)
    assertEquals("Simba Tembo", fetched?.name)
  }

  @Test
  fun observeUserProfile_authenticatedUser_emitsSavedProfile() = runBlocking {
    val uid = signInTestUser("sat_user_2@satlimited.com")
    val repository = UserRepository(firestore)

    val profile = UserProfileDocument(
      userId = uid,
      name = "SAT Executive",
      email = "sat_user_2@satlimited.com",
      organization = "SAT Limited",
      username = "@sat_exec",
      status = "Executive Account",
      phone = "+1 555 100 2000"
    )

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveUserProfile(profile).getOrThrow()
    }

    val emitted = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeUserProfile(uid).first { it != null }
    }
    assertNotNull(emitted)
    assertEquals("SAT Limited", emitted?.organization)
  }

  @Test
  fun unauthenticatedUser_access_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repository = UserRepository(firestore)

    val fetchResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.getUserProfile("unauthenticated_id")
    }
    assertTrue(fetchResult.isFailure)
    val exception = fetchResult.exceptionOrNull()
    assertNotNull(exception)
  }

  private companion object {
    const val DEFAULT_TIMEOUT_MS = 8000L
    const val FLOW_TIMEOUT_MS = 6000L
  }
}
