package com.sleepwell.fitness

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Analyzes vertical displacement of torso landmarks to compute respiratory rate.
 * Uses simple peak detection and signal filtering.
 *
 * NOT FOR MEDICAL USE - Wellness tracking only.
 */
class RespirationAnalyzer(
    private val windowSizeSeconds: Int = 45,
    private val samplingRateHz: Double = 10.0
) {

    data class RespirationResult(
        val breathsPerMinute: Double,
        val confidence: Float,
        val timestamp: Long = System.currentTimeMillis()
    )

    private val windowSize = (windowSizeSeconds * samplingRateHz).toInt()
    private val displacementBuffer = ArrayDeque<Float>(windowSize)
    private val timestampBuffer = ArrayDeque<Long>(windowSize)

    private val _respirationRate = MutableStateFlow<RespirationResult?>(null)
    val respirationRate: StateFlow<RespirationResult?> = _respirationRate

    // Bandpass filter parameters for breathing (0.1 - 0.8 Hz = 6-48 BPM)
    private val lowCutoffHz = 0.1
    private val highCutoffHz = 0.8

    /**
     * Add new displacement measurement
     * @param yPosition Vertical position of torso landmark (pixels or normalized)
     */
    fun addMeasurement(yPosition: Float) {
        val timestamp = System.currentTimeMillis()

        displacementBuffer.addLast(yPosition)
        timestampBuffer.addLast(timestamp)

        if (displacementBuffer.size > windowSize) {
            displacementBuffer.removeFirst()
            timestampBuffer.removeFirst()
        }

        if (displacementBuffer.size >= windowSize) {
            computeRespiratoryRate()
        }
    }

    private fun computeRespiratoryRate() {
        try {
            val signal = displacementBuffer.toFloatArray()

            // Detrend signal (remove linear trend)
            val detrended = detrend(signal)

            // Bandpass filter
            val filtered = bandpassFilter(detrended)

            // Detect peaks
            val peaks = detectPeaks(filtered)

            if (peaks.size < 2) {
                _respirationRate.value = RespirationResult(0.0, 0.0f)
                return
            }

            // Calculate average interval between peaks
            val intervals = peaks.zipWithNext { a, b -> b - a }
            val avgInterval = intervals.average()

            // Convert to breaths per minute
            val bpm = (60.0 * samplingRateHz) / avgInterval

            // Confidence based on signal quality
            val confidence = calculateConfidence(filtered, peaks)

            if (bpm in 6.0..48.0) { // Physiologically reasonable range
                _respirationRate.value = RespirationResult(bpm, confidence)
            }

        } catch (e: Exception) {
            _respirationRate.value = RespirationResult(0.0, 0.0f)
        }
    }

    private fun detrend(signal: FloatArray): FloatArray {
        val n = signal.size
        val mean = signal.average().toFloat()

        // Simple linear detrending
        val sumX = (0 until n).sum().toFloat()
        val sumY = signal.sum()
        val sumXY = signal.mapIndexed { i, y -> i * y }.sum()
        val sumX2 = (0 until n).sumOf { it * it }.toFloat()

        val slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
        val intercept = (sumY - slope * sumX) / n

        return FloatArray(n) { i ->
            signal[i] - (slope * i + intercept)
        }
    }

    private fun bandpassFilter(signal: FloatArray): FloatArray {
        // Simple moving average-based bandpass
        val lowpass = simpleMovingAverage(signal, (samplingRateHz / highCutoffHz).toInt())
        val highpass = signal.mapIndexed { i, v ->
            v - simpleMovingAverage(signal, (samplingRateHz / lowCutoffHz).toInt())[i]
        }.toFloatArray()

        return highpass
    }

    private fun simpleMovingAverage(data: FloatArray, windowSize: Int): FloatArray {
        val result = FloatArray(data.size)
        val halfWindow = windowSize / 2

        for (i in data.indices) {
            val start = maxOf(0, i - halfWindow)
            val end = minOf(data.size, i + halfWindow + 1)
            result[i] = data.sliceArray(start until end).average().toFloat()
        }

        return result
    }

    private fun detectPeaks(signal: FloatArray, threshold: Float = 0.3f): List<Int> {
        val peaks = mutableListOf<Int>()
        val std = calculateStd(signal)
        val mean = signal.average().toFloat()
        val minPeakHeight = mean + threshold * std

        for (i in 1 until signal.size - 1) {
            if (signal[i] > signal[i - 1] &&
                signal[i] > signal[i + 1] &&
                signal[i] > minPeakHeight) {

                // Ensure minimum distance between peaks (200ms)
                val minDistance = (0.2 * samplingRateHz).toInt()
                if (peaks.isEmpty() || i - peaks.last() >= minDistance) {
                    peaks.add(i)
                }
            }
        }

        return peaks
    }

    private fun calculateStd(data: FloatArray): Float {
        val mean = data.average().toFloat()
        val variance = data.map { (it - mean).pow(2) }.average().toFloat()
        return sqrt(variance)
    }

    private fun calculateConfidence(signal: FloatArray, peaks: List<Int>): Float {
        if (peaks.size < 2) return 0f

        // Confidence based on:
        // 1. Regularity of peak intervals
        val intervals = peaks.zipWithNext { a, b -> b - a }
        val intervalStd = intervals.map { it.toFloat() }.toFloatArray().let { calculateStd(it) }
        val intervalMean = intervals.average().toFloat()
        val regularity = 1f - (intervalStd / (intervalMean + 1f)).coerceIn(0f, 1f)

        // 2. Signal-to-noise ratio
        val peakValues = peaks.map { signal[it] }
        val peakMean = peakValues.average().toFloat()
        val signalPower = peakValues.map { it.pow(2) }.average().toFloat()
        val noisePower = signal.map { (it - peakMean).pow(2) }.average().toFloat()
        val snr = (signalPower / (noisePower + 0.001f)).coerceIn(0f, 10f) / 10f

        return (regularity * 0.6f + snr * 0.4f).coerceIn(0f, 1f)
    }

    fun reset() {
        displacementBuffer.clear()
        timestampBuffer.clear()
        _respirationRate.value = null
    }

    fun getMeasurementCount(): Int = displacementBuffer.size

    fun getProgress(): Float = (displacementBuffer.size.toFloat() / windowSize).coerceIn(0f, 1f)
}
