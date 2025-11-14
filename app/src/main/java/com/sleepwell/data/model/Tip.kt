package com.sleepwell.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Tip(
    val id: Int,
    val titleKey: String,
    val descriptionKey: String,
    val category: String,
    val iconResId: Int,
    val priority: Int = 0
) : Parcelable {
    companion object {
        // This will be populated with tips in the repository
        fun getSampleTips(): List<Tip> = emptyList()
    }
}

data class TipCategory(
    val id: String,
    val nameKey: String,
    val iconResId: Int,
    val colorHex: String
)
