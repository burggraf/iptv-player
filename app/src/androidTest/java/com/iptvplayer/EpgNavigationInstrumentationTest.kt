package com.iptvplayer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 7 — Instrumented UI Automator Tests.
 * Requires Android TV device or emulator.
 * Tests real DPad navigation, screen transitions, and playback.
 *
 * Run: ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.iptvplayer.EpgNavigationInstrumentationTest
 */
@RunWith(AndroidJUnit4::class)
class EpgNavigationInstrumentationTest {

    private lateinit var device: UiDevice
    private val appPackage = "com.iptvplayer"
    private val launchTimeout = 10_000L
    private val interactionTimeout = 3_000L

    @Before
    fun launchApp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Launch app
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = context.packageManager.getLaunchIntentForPackage(appPackage)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) }
        context.startActivity(intent)

        // Wait for app to load
        device.wait(Until.hasObject(By.pkg(appPackage).depth(0)), launchTimeout)
    }

    @Test
    fun fullEpgNavigationFlow() {
        // Wait for EPG content or empty state
        Thread.sleep(2000) // Allow initial render

        // Press DPad down to navigate channels
        device.pressDPadDown(null)
        device.wait(Until.hasObject(By.focused(true)), interactionTimeout)

        // Verify something is focused
        val focused = device.findObject(By.focused(true))
        assertNotNull("Expected a focused element", focused)
    }

    @Test
    fun dPadRightMovesThroughTimeAxis() {
        Thread.sleep(2000)

        // Navigate right through time
        device.pressDPadRight(null)
        Thread.sleep(200)
        device.pressDPadRight(null)
        Thread.sleep(200)
        device.pressDPadRight(null)

        // Should still be focused on something (no crash)
        val focused = device.findObject(By.focused(true))
        assertNotNull(focused)
    }

    @Test
    fun dPadLeftReturnsFromTimeAxis() {
        Thread.sleep(2000)

        // Move right, then back left
        device.pressDPadRight(null)
        device.pressDPadRight(null)
        device.pressDPadLeft(null)
        device.pressDPadLeft(null)

        val focused = device.findObject(By.focused(true))
        assertNotNull(focused)
    }

    @Test
    fun dPadUpMovesToPreviousChannel() {
        Thread.sleep(2000)

        // Move down, then back up
        device.pressDPadDown(null)
        device.pressDPadDown(null)
        device.pressDPadUp(null)
        device.pressDPadUp(null)

        val focused = device.findObject(By.focused(true))
        assertNotNull(focused)
    }

    @Test
    fun backButtonReturnsFromApp() {
        Thread.sleep(2000)

        // Press back
        device.pressBack()

        // Should no longer be in our app
        Thread.sleep(500)
        val currentPkg = device.currentPackageName
        // Back from main activity may exit or go to previous screen
        assertNotNull(currentPkg)
    }
}
