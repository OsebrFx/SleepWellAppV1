package com.sleepwell.fitness

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for HeartRateAnalyzer.
 * Tests PPG signal processing and BPM detection.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HeartRateAnalyzerTest {

    private lateinit var analyzer: HeartRateAnalyzer

    @Before
    fun setup() {
        analyzer = HeartRateAnalyzer(windowSeconds = 15, samplingRate = 30.0)
    }

    @Test
    fun `test synthetic PPG signal at 60 BPM`() = runTest {
        // Generate synthetic PPG signal at 60 BPM (1 Hz)
        val targetBpm = 60.0
        val frequencyHz = targetBpm / 60.0 // 1 Hz
        val samplingRate = 30.0
        val duration = 15
        val samples = (duration * samplingRate).toInt()

        // Generate sinusoidal signal simulating blood volume changes
        for (i in 0 until samples) {
            val time = i / samplingRate
            val signal = 128f + 20f * sin(2 * PI * frequencyHz * time).toFloat()
            analyzer.addMeasurement(signal)
        }

        kotlinx.coroutines.delay(100)

        val result = analyzer.heartRate.value
        assertNotNull(result, "Result should not be null")

        // Allow ±5 BPM tolerance
        val tolerance = 5.0
        assertTrue(
            result.beatsPerMinute in (targetBpm - tolerance)..(targetBpm + tolerance),
            "Expected BPM around $targetBpm, got ${result.beatsPerMinute}"
        )
    }

    @Test
    fun `test synthetic PPG signal at 75 BPM`() = runTest {
        // Generate synthetic PPG signal at 75 BPM (1.25 Hz)
        val targetBpm = 75.0
        val frequencyHz = targetBpm / 60.0
        val samplingRate = 30.0
        val duration = 15
        val samples = (duration * samplingRate).toInt()

        for (i in 0 until samples) {
            val time = i / samplingRate
            val signal = 128f + 15f * sin(2 * PI * frequencyHz * time).toFloat()
            val noise = (Math.random() * 4 - 2).toFloat()
            analyzer.addMeasurement(signal + noise)
        }

        kotlinx.coroutines.delay(100)

        val result = analyzer.heartRate.value
        assertNotNull(result)

        val tolerance = 5.0
        assertTrue(
            result.beatsPerMinute in (targetBpm - tolerance)..(targetBpm + tolerance),
            "Expected BPM around $targetBpm, got ${result.beatsPerMinute}"
        )
    }

    @Test
    fun `test progress tracking`() {
        val samplingRate = 30.0
        val windowSeconds = 15
        val totalSamples = (windowSeconds * samplingRate).toInt()

        // Add quarter of the samples
        repeat(totalSamples / 4) {
            analyzer.addMeasurement(128f)
        }

        val progress = analyzer.getProgress()
        assertTrue(progress >= 0.2f && progress <= 0.3f,
            "Progress should be around 0.25, got $progress")
    }

    @Test
    fun `test reset functionality`() = runTest {
        // Add measurements
        repeat(100) {
            analyzer.addMeasurement(128f + it.toFloat())
        }

        // Reset
        analyzer.reset()

        // Progress should be 0
        assertTrue(analyzer.getProgress() < 0.01f)

        // Result should be null
        val result = analyzer.heartRate.value
        assertTrue(result == null || result.beatsPerMinute == 0.0)
    }

    @Test
    fun `test physiological range validation`() = runTest {
        // Try impossibly fast heart rate (250 BPM = 4.17 Hz)
        val impossibleBpm = 250.0
        val frequencyHz = impossibleBpm / 60.0
        val samplingRate = 30.0
        val duration = 15
        val samples = (duration * samplingRate).toInt()

        for (i in 0 until samples) {
            val time = i / samplingRate
            val signal = 128f + 20f * sin(2 * PI * frequencyHz * time).toFloat()
            analyzer.addMeasurement(signal)
        }

        kotlinx.coroutines.delay(100)

        val result = analyzer.heartRate.value
        assertNotNull(result)

        // Should be rejected (outside 40-200 BPM range)
        assertTrue(result.beatsPerMinute in 40.0..200.0 || result.beatsPerMinute == 0.0,
            "BPM should be in valid range or 0, got ${result.beatsPerMinute}")
    }

    @Test
    fun `test flat signal returns zero BPM`() = runTest {
        // Constant signal (no pulse)
        repeat(450) {
            analyzer.addMeasurement(128f)
        }

        kotlinx.coroutines.delay(100)

        val result = analyzer.heartRate.value
        assertNotNull(result)

        // Should return 0 BPM for flat signal
        assertTrue(result.beatsPerMinute == 0.0 || result.confidence < 0.2f,
            "Flat signal should have 0 BPM or very low confidence")
    }
}
