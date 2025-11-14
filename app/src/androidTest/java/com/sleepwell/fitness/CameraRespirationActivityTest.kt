package com.sleepwell.fitness

import android.Manifest
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.sleepwell.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation test for CameraRespirationActivity.
 * Verifies the respiratory rate measurement flow.
 */
@RunWith(AndroidJUnit4::class)
class CameraRespirationActivityTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )

    @Test
    fun testActivityLaunches() {
        // Launch activity
        val scenario = ActivityScenario.launch(CameraRespirationActivity::class.java)

        // Verify camera preview is displayed
        onView(withId(R.id.previewView))
            .check(matches(isDisplayed()))

        // Verify instructions card is displayed
        onView(withId(R.id.instructionsCard))
            .check(matches(isDisplayed()))

        // Verify results card is displayed
        onView(withId(R.id.resultsCard))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun testCloseButtonWorks() {
        val scenario = ActivityScenario.launch(CameraRespirationActivity::class.java)

        // Click close button
        onView(withId(R.id.btnClose))
            .check(matches(isDisplayed()))
            .perform(click())

        // Activity should be finishing/closed
        scenario.onActivity { activity ->
            assert(activity.isFinishing || activity.isDestroyed)
        }

        scenario.close()
    }

    @Test
    fun testInitialBpmDisplay() {
        val scenario = ActivityScenario.launch(CameraRespirationActivity::class.java)

        // Initial BPM should show "--"
        onView(withId(R.id.tvBpm))
            .check(matches(isDisplayed()))
            .check(matches(withText("--")))

        // Progress should be visible
        onView(withId(R.id.progressBar))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun testProgressBarExists() {
        val scenario = ActivityScenario.launch(CameraRespirationActivity::class.java)

        // Verify progress bar is displayed
        onView(withId(R.id.progressBar))
            .check(matches(isDisplayed()))

        // Verify progress text is displayed
        onView(withId(R.id.tvProgress))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun testResetButtonInitiallyHidden() {
        val scenario = ActivityScenario.launch(CameraRespirationActivity::class.java)

        // Reset button should be gone initially
        onView(withId(R.id.btnReset))
            .check(matches(withEffectiveVisibility(Visibility.GONE)))

        scenario.close()
    }

    @Test
    fun testInstructionTextDisplayed() {
        val scenario = ActivityScenario.launch(CameraRespirationActivity::class.java)

        // Verify instruction title is displayed
        onView(withText(R.string.rr_instruction_title))
            .check(matches(isDisplayed()))

        // Verify instruction description is displayed
        onView(withText(R.string.rr_instruction_desc))
            .check(matches(isDisplayed()))

        scenario.close()
    }

    @Test
    fun testMedicalDisclaimerDisplayed() {
        val scenario = ActivityScenario.launch(CameraRespirationActivity::class.java)

        // Verify medical disclaimer is displayed
        onView(withText(R.string.rr_medical_disclaimer))
            .check(matches(isDisplayed()))

        scenario.close()
    }
}
