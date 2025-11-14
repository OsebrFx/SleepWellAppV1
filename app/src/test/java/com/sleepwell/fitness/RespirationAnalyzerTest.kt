package com.sleepwell.fitness

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for RespirationAnalyzer.
 * Tests synthetic displacement signals and BPM detection accuracy.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RespirationAnalyzerTest {

    private lateinit var analyzer: RespirationAnalyzer

    @Before
    fun setup() {
        analyzer = RespirationAnalyzer(windowSeconds = 45, samplingRate = 10.0)
    }

    @Test
    fun `test synthetic signal at 12 breaths per minute`() = runTest {
        // Generate synthetic breathing signal at 12 BPM (0.2 Hz)
        val targetBpm = 12.0
        val frequencyHz = targetBpm / 60.0 // 0.2 Hz
        val samplingRate = 10.0
        val duration = 45 // seconds
        val samples = (duration * samplingRate).toInt()

        // Generate sinusoidal signal with noise
        for (i in 0 until samples) {
            val time = i / samplingRate
            val signal = 100f + 10f * sin(2 * PI * frequencyHz * time).toFloat()
            val noise = (Math.random() * 2 - 1).toFloat() // ±1 noise
            analyzer.addMeasurement(signal + noise)
        }

        // Wait for analysis to complete
        kotlinx.coroutines.delay(100)

        // Get result
        val result = analyzer.respirationRate.value
        assertNotNull(result, "Result should not be null after full buffer")

        // Allow ±2 BPM tolerance
        val tolerance = 2.0
        assertTrue(
            result.breathsPerMinute in (targetBpm - tolerance)..(targetBpm + tolerance),
            "Expected BPM around $targetBpm, got ${result.breathsPerMinute}"
        )

        // Confidence should be reasonable
        assertTrue(result.confidence > 0.3f, "Confidence should be > 0.3, got ${result.confidence}")
    }

    @Test
    fun `test synthetic signal at 18 breaths per minute`() = runTest {
        // Generate synthetic breathing signal at 18 BPM (0.3 Hz)
        val targetBpm = 18.0
        val frequencyHz = targetBpm / 60.0
        val samplingRate = 10.0
        val duration = 45
        val samples = (duration * samplingRate).toInt()

        for (i in 0 until samples) {
            val time = i / samplingRate
            val signal = 100f + 8f * sin(2 * PI * frequencyHz * time).toFloat()
            analyzer.addMeasurement(signal)
        }

        kotlinx.coroutines.delay(100)

        val result = analyzer.respirationRate.value
        assertNotNull(result, "Result should not be null")

        val tolerance = 2.0
        assertTrue(
            result.breathsPerMinute in (targetBpm - tolerance)..(targetBpm + tolerance),
            "Expected BPM around $targetBpm, got ${result.breathsPerMinute}"
        )
    }

    @Test
    fun `test progress tracking`() {
        val samplingRate = 10.0
        val windowSeconds = 45
        val totalSamples = (windowSeconds * samplingRate).toInt()

        // Add half the samples
        repeat(totalSamples / 2) {
            analyzer.addMeasurement(100f)
        }

        val progress = analyzer.getProgress()
        assertTrue(progress >= 0.45f && progress <= 0.55f, "Progress should be around 0.5, got $progress")
    }

    @Test
    fun `test reset functionality`() = runTest {
        // Add some measurements
        repeat(100) {
            analyzer.addMeasurement(100f + it.toFloat())
        }

        // Reset
        analyzer.reset()

        // Progress should be back to 0
        assertEquals(0f, analyzer.getProgress(), 0.01f)

        // Result should be null
        val result = analyzer.respirationRate.value
        assertEquals(null, result)
    }

    @Test
    fun `test invalid signal returns zero BPM`() = runTest {
        // Constant signal (no breathing movement)
        repeat(450) {
            analyzer.addMeasurement(100f)
        }

        kotlinx.coroutines.delay(100)

        val result = analyzer.respirationRate.value
        assertNotNull(result)

        // Should return 0 BPM for invalid/flat signal
        assertTrue(result.breathsPerMinute == 0.0 || result.confidence < 0.2f,
            "Flat signal should have 0 BPM or very low confidence")
    }

    @Test
    fun `test physiological range validation`() = runTest {
        // Try to generate impossibly fast breathing (100 BPM = 1.67 Hz)
        val impossibleBpm = 100.0
        val frequencyHz = impossibleBpm / 60.0
        val samplingRate = 10.0
        val duration = 45
        val samples = (duration * samplingRate).toInt()

        for (i in 0 until samples) {
            val time = i / samplingRate
            val signal = 100f + 10f * sin(2 * PI * frequencyHz * time).toFloat()
            analyzer.addMeasurement(signal)
        }

        kotlinx.coroutines.delay(100)

        val result = analyzer.respirationRate.value
        assertNotNull(result)

        // Should be rejected as physiologically invalid (outside 6-48 BPM range)
        assertTrue(result.breathsPerMinute in 6.0..48.0 || result.breathsPerMinute == 0.0,
            "BPM should be in valid range or 0, got ${result.breathsPerMinute}")
    }

    @Test
    fun `test irregular breathing pattern`() = runTest {
        // Generate irregular breathing with varying frequency
        val samplingRate = 10.0
        val duration = 45
        val samples = (duration * samplingRate).toInt()

        for (i in 0 until samples) {
            val time = i / samplingRate
            // Mix of two frequencies: 0.2 Hz and 0.25 Hz
            val signal1 = 5f * sin(2 * PI * 0.2 * time).toFloat()
            val signal2 = 3f * sin(2 * PI * 0.25 * time).toFloat()
            analyzer.addMeasurement(100f + signal1 + signal2)
        }

        kotlinx.coroutines.delay(100)

        val result = analyzer.respirationRate.value
        assertNotNull(result)

        // Confidence should be lower for irregular patterns
        assertTrue(result.confidence < 0.7f,
            "Irregular pattern should have lower confidence, got ${result.confidence}")
    }
}
