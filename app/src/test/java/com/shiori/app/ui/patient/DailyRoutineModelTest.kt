package com.shiori.app.ui.patient

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyRoutineModelTest {
    data class RoutineItem(val id: String, val title: String, val isCompleted: Boolean)

    private fun calculateCompletion(items: List<RoutineItem>): Float {
        if (items.isEmpty()) return 0f
        val completed = items.count { it.isCompleted }
        return completed.toFloat() / items.size
    }

    @Test
    fun testCompletionPercentage() {
        val items = listOf(
            RoutineItem("1", "Walk", true),
            RoutineItem("2", "Water", true),
            RoutineItem("3", "Meds", false),
            RoutineItem("4", "Sleep", false)
        )
        assertEquals(0.5f, calculateCompletion(items), 0.01f)
    }

    @Test
    fun testEmptyItemsReturnZero() {
        assertEquals(0f, calculateCompletion(emptyList()), 0.01f)
    }

    @Test
    fun testAllCompletedReturnsOne() {
        val items = listOf(
            RoutineItem("1", "Walk", true),
            RoutineItem("2", "Water", true)
        )
        assertEquals(1.0f, calculateCompletion(items), 0.01f)
    }
}
