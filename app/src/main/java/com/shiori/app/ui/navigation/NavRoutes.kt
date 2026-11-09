package com.shiori.app.ui.navigation

sealed class NavRoutes(val route: String) {
    object PatientHome : NavRoutes("patient_home")
    object CaregiverPin : NavRoutes("caregiver_pin")
    object CaregiverDashboard : NavRoutes("caregiver_dashboard")
    object MemoryMatch : NavRoutes("memory_match")
    object PatternSequence : NavRoutes("pattern_sequence")
    object FaceRecall : NavRoutes("face_recall")
}
