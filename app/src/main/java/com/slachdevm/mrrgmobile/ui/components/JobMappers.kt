package com.slachdevm.mrrgmobile.ui.components

import com.slachdevm.mrrgmobile.domain.model.JobStatus
import com.slachdevm.mrrgmobile.domain.model.UserRole

fun String.toJobTypeLabel(): String {
    if (this.isBlank()) return "Unknown Type"
    
    return this.split(",")
        .map { type ->
            when (type.trim().uppercase()) {
                "FULL_RESHEET" -> "Full Resheet"
                "FULL_REFASCIA" -> "Full Refascia"
                "RE_GUTTER" -> "Re-Gutter"
                "CLEANING" -> "Cleaning"
                "REPAIR" -> "Repair"
                "MAINTENANCE" -> "Maintenance"
                "RESCREW" -> "Rescrew"
                "GUTTER_GUARD" -> "Gutter Guard"
                "VALLEY_REPLACEMENT" -> "Valley Replacement"
                "DOWNPIPE_INSTALL" -> "Downpipe Install"
                else -> type.trim()
                    .replace("_", " ")
                    .lowercase()
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            }
        }
        .joinToString(", ")
}

fun JobStatus.toLabel(): String = this.displayName

fun UserRole.toLabel(): String = this.displayName

fun Int.toPriorityLabel(): String {
    return when (this) {
        1 -> "Low"
        2 -> "Medium"
        3 -> "High"
        4 -> "Urgent / Callback"
        else -> "Normal"
    }
}
