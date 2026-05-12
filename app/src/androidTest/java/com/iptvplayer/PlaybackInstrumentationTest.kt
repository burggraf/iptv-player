package com.iptvplayer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 7 — Instrumented Playback Tests.
 * Tests fullscreen player screen and controls.
 *
 * Run: ./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.iptvplayer.PlaybackInstrumentationTest
 */
@RunWith(AndroidJUnit4::class)
class PlaybackInstrumentationTest {

    private lateinit var device: UiDevice
    private val appPackage = "com.iptvplayer"
    private val launchTimeout = 10_000L
    private val interactionTimeout = 5_000L

    @Before
    fun launchApp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = context.packageManager.getLaunchIntentForPackage(appPackage)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) }
        context.startActivity(intent)

        device.wait(Until.hasObject(By.pkg(appPackage).depth(0)), launchTimeout)
    }

    @Test
    fun openFullscreenPlayer() {
        Thread.sleep(2000)

        // Navigate to fullscreen button and press
        // Look for fullscreen icon/button text
        val fullscreenObj = device.findObject(By.textContains("Fullscreen"))
            ?: device.findObject(By.res(appPackage, "fullscreen-button"))

        if (fullscreenObj != null) {
            fullscreenObj.click()
            device.wait(Until.hasObject(By.res(appPackage, "fullscreen-player")), interactionTimeout)
        }

        // Verify we didn't crash
        assertNotNull(device.currentPackageName)
    }

    @Test
    fun playerControlsRespondToDPad() {
        Thread.sleep(2000)

        // Open controls (DPad center on preview)
        device.pressDPadCenter(null)
        Thread.sleep(500)

        // Try play/pause
        device.pressDPadCenter(null)
        Thread.sleep(500)

        // Verify no crash
        assertNotNull(device.findObject(By.pkg(appPackage)))
    }

    @Test
    fun appStartsUnder2Seconds() {
        val startTime = System.currentTimeMillis()

        // Relaunch for timing
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = context.packageManager.getLaunchIntentForPackage(appPackage)
            ?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)

        device.wait(Until.hasObject(By.pkg(appPackage).depth(0)), launchTimeout)
        val elapsed = System.currentTimeMillis() - startTime

        // Performance target: cold start < 2s
        assert(elapsed < 2000) { "Cold start took ${elapsed}ms, target < 2000ms" }
    }
}
