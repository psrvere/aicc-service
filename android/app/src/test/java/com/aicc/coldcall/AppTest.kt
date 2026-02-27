package com.aicc.coldcall

import org.junit.Assert.assertEquals
import org.junit.Test

class AppTest {
    @Test
    fun `app module has correct application id`() {
        assertEquals("com.aicc.coldcall", BuildConfig.APPLICATION_ID)
    }
}
