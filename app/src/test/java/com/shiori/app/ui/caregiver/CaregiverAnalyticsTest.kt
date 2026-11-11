package com.shiori.app.ui.caregiver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CaregiverAnalyticsTest {
    private fun validatePin(entered: String, target: String = "1234"): Boolean = entered == target

    private fun computeAdherence(taken: Int, total: Int): Float {
        if (total == 0) return 1.0f
        return taken.toFloat() / total
    }

    @Test
    fun testPinValidation() {
        assertTrue(validatePin("1234"))
        assertFalse(validatePin("0000"))
        assertFalse(validatePin("123"))
    }

    @Test
    fun testAdherenceCalculation() {
        assertEquals(0.75f, computeAdherence(3, 4), 0.01f)
        assertEquals(1.0f, computeAdherence(0, 0), 0.01f)
    }
}
