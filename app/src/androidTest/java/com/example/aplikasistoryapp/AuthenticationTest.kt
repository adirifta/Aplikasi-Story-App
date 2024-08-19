@file:Suppress("DEPRECATION")

package com.example.aplikasistoryapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import com.example.aplikasistoryapp.ui.activity.LoginActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticationTest {

    @get:Rule
    val activityRule = ActivityTestRule(LoginActivity::class.java)

    private lateinit var idlingResource: IdlingResource

    @Before
    fun setup() {
        idlingResource = GeneralIdlingResource(activityRule.activity)
        IdlingRegistry.getInstance().register(idlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    @Test
    fun testLogin() {
        // Perform login
        onView(withId(R.id.ed_login_email)).perform(replaceText("adii@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(replaceText("12345678"), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).perform(click())

        // Wait for login to complete
        // Using IdlingResource to wait instead of Thread.sleep()

        // Verify successful login by checking if the next activity or toast is displayed
        onView(withId(R.id.action_settings)).check(matches(isDisplayed()))  // Replace with actual check
    }

    @Test
    fun testLogout() {
        // First ensure user is logged in
        testLogin()

        // Navigate to SettingsActivity
        onView(withId(R.id.action_settings)).perform(click())

        // Perform logout
        onView(withId(R.id.logoutButton)).perform(click())

        // Verify navigation to LoginActivity
        onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))  // Assuming login email field is visible on LoginActivity
    }
}