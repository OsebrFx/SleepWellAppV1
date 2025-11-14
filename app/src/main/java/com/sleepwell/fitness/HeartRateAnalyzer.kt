package com.sleepwell.fitness

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Analyzes red channel intensity from camera frames to calculate heart rate using PPG
 * (photoplethysmography) technique.
 *
 * Expected usage:
 * 1. User places fingertip over back camera with flash on
 * 2. Add red channel average values at ~30 FPS for 15 seconds
 * 3. Signal processing extracts heart rate from periodic variations
 *
 * Physiologically valid range: 40-200 BPM
 */
class HeartRateAnalyzer(
    private val windowSeconds: Int = 15,
    private val samplingRate: Double = 30.0 // 30 FPS
) {
    data class HeartRateResult(
        val beatsPerMinute: Double,
        val confidence: Float, // 0.0 to 1.0
        val timestamp: Long = System.currentTimeMillis()
    )

    private val bufferSize = (windowSeconds * samplingRate).toInt()
    private val buffer = FloatArray(bufferSize)
    private var currentIndex = 0
    private var isBufferFull = false

    private val _heartRate = MutableStateFlow<HeartRateResult?>(null)
    val heartRate: StateFlow<HeartRateResult?> = _heartRate.asStateFlow()

    /**
     * Adds a red channel intensity measurement (0-255 range)
     * @param redValue Average red channel intensity from camera frame
     */
    fun addMeasurement(redValue: Float) {
        buffer[currentIndex] = redValue
        currentIndex++

        if (currentIndex >= bufferSize) {
            currentIndex = 0
            isBufferFull = true
        }

        if (isBufferFull) {
            computeHeartRate()
        }
    }

    private fun computeHeartRate() {
        try {
            // 1. Get signal array
            val signal = if (isBufferFull) {
                buffer.copyOf()
            } else {
                buffer.copyOfRange(0, currentIndex)
            }

            if (signal.size < 100) return // Need at least ~3 seconds of data

            // 2. Detrend to remove DC component
            val detrended = detrend(signal)

            // 3. Bandpass filter: 0.5-3 Hz (30-180 BPM)
            val filtered = bandpassFilter(detrended, lowCutoff = 0.5, highCutoff = 3.0)

            // 4. Detect peaks
            val peaks = detectPeaks(filtered)

            if (peaks.size < 3) {
                _heartRate.value = HeartRateResult(0.0, 0f)
                return
            }

            // 5. Calculate heart rate from peak intervals
            val intervals = peaks.zipWithNext { a, b -> b - a }
            val avgInterval = intervals.average()
            val bpm = (60.0 * samplingRate) / avgInterval

            // 6. Validate physiologically (40-200 BPM)
            if (bpm < 40.0 || bpm > 200.0) {
                _heartRate.value = HeartRateResult(0.0, 0f)
                return
            }

            // 7. Calculate confidence
            val confidence = calculateConfidence(filtered, peaks, intervals)

            _heartRate.value = HeartRateResult(bpm, confidence)

        } catch (e: Exception) {
            e.printStackTrace()
            _heartRate.value = HeartRateResult(0.0, 0f)
        }
    }

    private fun detrend(signal: FloatArray): FloatArray {
        val mean = signal.average().toFloat()
        return FloatArray(signal.size) { i -> signal[i] - mean }
    }

    private fun bandpassFilter(
        signal: FloatArray,
        lowCutoff: Double,
        highCutoff: Double
    ): FloatArray {
        // Simple moving average approximation for bandpass
        // High-pass: remove slow trends (>3 Hz)
        val highPassWindowSize = (samplingRate / highCutoff).toInt().coerceAtLeast(3)
        val highPassed = FloatArray(signal.size)

        for (i in signal.indices) {
            val start = (i - highPassWindowSize / 2).coerceAtLeast(0)
            val end = (i + highPassWindowSize / 2).coerceAtMost(signal.size - 1)
            val windowMean = signal.sliceArray(start..end).average().toFloat()
            highPassed[i] = signal[i] - windowMean
        }

        // Low-pass: remove fast noise (<0.5 Hz)
        val lowPassWindowSize = (samplingRate / lowCutoff).toInt().coerceAtLeast(3)
        val filtered = FloatArray(signal.size)

        for (i in highPassed.indices) {
            val start = (i - lowPassWindowSize / 2).coerceAtLeast(0)
            val end = (i + lowPassWindowSize / 2).coerceAtMost(highPassed.size - 1)
            filtered[i] = highPassed.sliceArray(start..end).average().toFloat()
        }

        return filtered
    }

    private fun detectPeaks(signal: FloatArray, threshold: Float = 0.4f): List<Int> {
        val peaks = mutableListOf<Int>()
        val stdDev = calculateStdDev(signal)
        val mean = signal.average().toFloat()
        val peakThreshold = mean + threshold * stdDev

        // Minimum distance between peaks (0.3 seconds = 200 BPM max)
        val minPeakDistance = (samplingRate * 0.3).toInt()

        for (i in 1 until signal.size - 1) {
            if (signal[i] > signal[i - 1] &&
                signal[i] > signal[i + 1] &&
                signal[i] > peakThreshold) {

                // Check minimum distance from last peak
                if (peaks.isEmpty() || i - peaks.last() >= minPeakDistance) {
                    peaks.add(i)
                }
            }
        }

        return peaks
    }

    private fun calculateConfidence(
        signal: FloatArray,
        peaks: List<Int>,
        intervals: List<Int>
    ): Float {
        if (peaks.size < 3) return 0f

        // 1. Peak regularity (60%): how consistent are the intervals?
        val intervalMean = intervals.average()
        val intervalStdDev = sqrt(intervals.map { (it - intervalMean) * (it - intervalMean) }.average()).toFloat()
        val regularityScore = (1f - (intervalStdDev / intervalMean.toFloat()).coerceIn(0f, 1f))

        // 2. Signal-to-noise ratio (40%): peak amplitude vs noise
        val peakValues = peaks.map { signal[it] }
        val peakMean = peakValues.average().toFloat()
        val signalStdDev = calculateStdDev(signal)
        val snr = if (signalStdDev > 0) (peakMean / signalStdDev).coerceIn(0f, 5f) / 5f else 0f

        return (0.6f * regularityScore + 0.4f * snr).coerceIn(0f, 1f)
    }

    private fun calculateStdDev(values: FloatArray): Float {
        val mean = values.average().toFloat()
        val variance = values.map { (it - mean) * (it - mean) }.average().toFloat()
        return sqrt(variance)
    }

    fun getProgress(): Float {
        return if (isBufferFull) {
            1f
        } else {
            currentIndex.toFloat() / bufferSize
        }
    }

    fun reset() {
        currentIndex = 0
        isBufferFull = false
        buffer.fill(0f)
        _heartRate.value = null
    }
}
