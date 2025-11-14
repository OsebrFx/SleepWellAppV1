package com.sleepwell.fitness

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Helper to extract stable torso/shoulder landmarks for respiration tracking.
 * Prioritizes: chest center, shoulders, hips - uses best available.
 */
object PoseHelper {

    /**
     * Extract average vertical position of torso landmarks.
     * Returns null if no valid landmarks found.
     */
    fun extractTorsoYPosition(pose: Pose): Float? {
        val landmarks = listOfNotNull(
            pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
            pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER),
            pose.getPoseLandmark(PoseLandmark.LEFT_HIP),
            pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        )

        if (landmarks.isEmpty()) return null

        // Filter by confidence
        val validLandmarks = landmarks.filter { it.inFrameLikelihood > 0.5f }

        if (validLandmarks.isEmpty()) return null

        // Return weighted average Y position
        val totalConfidence = validLandmarks.sumOf { it.inFrameLikelihood.toDouble() }.toFloat()
        val weightedSum = validLandmarks.sumOf {
            (it.position.y * it.inFrameLikelihood).toDouble()
        }.toFloat()

        return weightedSum / totalConfidence
    }

    /**
     * Check if pose is valid for respiration tracking.
     * User should be relatively still with torso visible.
     */
    fun isPoseValid(pose: Pose): Boolean {
        val shoulders = listOfNotNull(
            pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
            pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        )

        return shoulders.size == 2 &&
                shoulders.all { it.inFrameLikelihood > 0.6f }
    }

    /**
     * Calculate pose stability score (0-1).
     * Higher is more stable.
     */
    fun calculateStability(currentPose: Pose, previousPose: Pose?): Float {
        if (previousPose == null) return 1f

        val currentY = extractTorsoYPosition(currentPose) ?: return 0f
        val previousY = extractTorsoYPosition(previousPose) ?: return 0f

        val movement = kotlin.math.abs(currentY - previousY)

        // Movement < 5 pixels is very stable
        return (1f - (movement / 50f)).coerceIn(0f, 1f)
    }
}
