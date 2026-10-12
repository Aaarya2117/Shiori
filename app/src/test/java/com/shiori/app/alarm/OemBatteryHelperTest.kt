package com.shiori.app.alarm

import android.content.Context
import android.content.ContextWrapper
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OemBatteryHelperTest {
    private lateinit var context: Context
    private lateinit var helper: OemBatteryHelper

    @Before
    fun setUp() {
        context = object : ContextWrapper(null) {
            override fun getPackageName(): String = "com.shiori.app"
        }
        helper = OemBatteryHelper(context)
    }

    @Test
    fun testGetVendorAutostartIntentsNotNull() {
        val intents = helper.getVendorAutostartIntents()
        assertNotNull(intents)
        // Fallback intent should always be included
        assertTrue(intents.isNotEmpty())
    }

    @Test
    fun testIsBatteryOptimizationIgnoredReturnsBoolean() {
        val isIgnored = helper.isBatteryOptimizationIgnored()
        assertNotNull(isIgnored)
    }
}
