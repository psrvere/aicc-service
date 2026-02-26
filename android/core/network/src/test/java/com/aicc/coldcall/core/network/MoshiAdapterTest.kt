package com.aicc.coldcall.core.network

import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.model.Disposition
import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Test

class MoshiAdapterTest {

    private val moshi: Moshi = MoshiFactory.create()

    @Test
    fun `serialize DealStage to JSON string`() {
        val adapter = moshi.adapter(DealStage::class.java)
        assertEquals("\"New\"", adapter.toJson(DealStage.New))
        assertEquals("\"NotInterested\"", adapter.toJson(DealStage.NotInterested))
    }

    @Test
    fun `deserialize JSON string to DealStage`() {
        val adapter = moshi.adapter(DealStage::class.java)
        assertEquals(DealStage.Qualified, adapter.fromJson("\"Qualified\""))
        assertEquals(DealStage.Won, adapter.fromJson("\"Won\""))
    }

    @Test
    fun `serialize Disposition to JSON string`() {
        val adapter = moshi.adapter(Disposition::class.java)
        assertEquals("\"Connected\"", adapter.toJson(Disposition.Connected))
        assertEquals("\"WrongNumber\"", adapter.toJson(Disposition.WrongNumber))
    }

    @Test
    fun `deserialize JSON string to Disposition`() {
        val adapter = moshi.adapter(Disposition::class.java)
        assertEquals(Disposition.NoAnswer, adapter.fromJson("\"NoAnswer\""))
        assertEquals(Disposition.Callback, adapter.fromJson("\"Callback\""))
    }

    @Test
    fun `all DealStage values round-trip through JSON`() {
        val adapter = moshi.adapter(DealStage::class.java)
        DealStage.values().forEach { stage ->
            val json = adapter.toJson(stage)
            val deserialized = adapter.fromJson(json)
            assertEquals(stage, deserialized)
        }
    }

    @Test
    fun `all Disposition values round-trip through JSON`() {
        val adapter = moshi.adapter(Disposition::class.java)
        Disposition.values().forEach { disp ->
            val json = adapter.toJson(disp)
            val deserialized = adapter.fromJson(json)
            assertEquals(disp, deserialized)
        }
    }
}
