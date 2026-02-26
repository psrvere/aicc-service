package com.aicc.coldcall.core.database

import androidx.room.TypeConverter
import com.aicc.coldcall.core.model.DealStage
import com.aicc.coldcall.core.model.Disposition

class Converters {
    @TypeConverter
    fun fromDealStage(value: DealStage): String = value.name

    @TypeConverter
    fun toDealStage(value: String): DealStage = DealStage.valueOf(value)

    @TypeConverter
    fun fromDisposition(value: Disposition): String = value.name

    @TypeConverter
    fun toDisposition(value: String): Disposition = Disposition.valueOf(value)
}
