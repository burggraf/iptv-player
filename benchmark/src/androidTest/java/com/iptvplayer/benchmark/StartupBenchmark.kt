package com.iptvplayer.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 7 — Macrobenchmark for cold start and EPG grid scrolling.
 * Run: ./gradlew :benchmark:connectedBenchmarkAndroidTest
 *
 * Targets:
 * - Cold start < 2s
 * - EPG grid scroll: 60fps
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldStartup() {
        benchmarkRule.measureRepeated(
            packageName = "com.iptvplayer",
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            startupMode = StartupMode.COLD,
            iterations = 5,
        ) {
            startActivityAndWait()
        }
    }

    @Test
    fun coldStartup_full() {
        benchmarkRule.measureRepeated(
            packageName = "com.iptvplayer",
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.Full(),
            startupMode = StartupMode.COLD,
            iterations = 5,
        ) {
            startActivityAndWait()
        }
    }
}
