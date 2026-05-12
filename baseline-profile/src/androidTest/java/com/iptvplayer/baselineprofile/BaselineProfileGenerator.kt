package com.iptvplayer.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 7 — Baseline Profile generator.
 * Generates profiles for:
 * - App startup (critical path)
 * - EPG grid scrolling
 * - Channel switching
 *
 * Run: ./gradlew :baseline-profile:generateBenchmarkBaselineProfile
 * Output: app/src/main/baseline-prof.txt
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = "com.iptvplayer",
            includeInStartupProfile = true,
            stableIterations = 2,
            maxIterations = 8,
        ) {
            // Critical path: app startup → EPG screen
            startActivityAndWait()

            device.wait(Until.hasObject(By.pkg("com.iptvplayer").depth(0)), 5_000)

            // Navigate EPG grid (DPad scrolling)
            repeat(10) {
                device.pressDPadDown()
                Thread.sleep(100)
            }
            repeat(10) {
                device.pressDPadUp()
                Thread.sleep(100)
            }

            // Horizontal scroll through time
            repeat(5) {
                device.pressDPadRight()
                Thread.sleep(100)
            }
            repeat(5) {
                device.pressDPadLeft()
                Thread.sleep(100)
            }

            // Select a channel
            device.pressDPadCenter()
            Thread.sleep(500)
        }
    }
}
