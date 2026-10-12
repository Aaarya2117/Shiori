package com.shiori.app.data.local.converter

import com.shiori.app.domain.model.ReminderStatus
import com.shiori.app.domain.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun testIntListConversion() {
        val list = listOf(1, 2, 4, 7)
        val stringRep = converters.fromIntList(list)
        assertEquals("1,2,4,7", stringRep)

        val restored = converters.toIntList(stringRep)
        assertEquals(list, restored)

        assertTrue(converters.toIntList("").isEmpty())
        assertTrue(converters.toIntList(null).isEmpty())
    }

    @Test
    fun testSyncStatusConversion() {
        assertEquals("PENDING", converters.fromSyncStatus(SyncStatus.PENDING))
        assertEquals(SyncStatus.SYNCED, converters.toSyncStatus("SYNCED"))
        assertEquals(SyncStatus.PENDING, converters.toSyncStatus("INVALID_STATUS"))
    }

    @Test
    fun testReminderStatusConversion() {
        assertEquals("TRIGGERED", converters.fromReminderStatus(ReminderStatus.TRIGGERED))
        assertEquals(ReminderStatus.TAKEN, converters.toReminderStatus("TAKEN"))
        assertEquals(ReminderStatus.SCHEDULED, converters.toReminderStatus("UNKNOWN"))
    }
}
