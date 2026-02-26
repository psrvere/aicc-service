package com.aicc.coldcall.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelTest {
    @Test
    fun `core model module compiles and tests run`() {
        assertTrue("Module should compile and run tests", true)
    }

    @Test
    fun `DealStage has 8 values`() {
        assertEquals(8, DealStage.values().size)
    }

    @Test
    fun `Disposition has 6 values`() {
        assertEquals(6, Disposition.values().size)
    }
}
