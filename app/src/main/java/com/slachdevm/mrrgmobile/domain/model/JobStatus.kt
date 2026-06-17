package com.slachdevm.mrrgmobile.domain.model

enum class JobStatus(val displayName: String) {
    PENDING("Pending"),
    SCHEDULED("Scheduled"),
    READY_FOR_CONFIRMATION("Ready for Confirmation"),
    DONE("Completed"),
    TO_BE_FIXED("To Be Fixed"),
    ARCHIVED("Archived")
}
