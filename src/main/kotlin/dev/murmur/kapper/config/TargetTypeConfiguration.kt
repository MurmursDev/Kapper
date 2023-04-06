package dev.murmur.kapper.config

import com.google.devtools.ksp.symbol.KSType

data class TargetTypeConfiguration(
    val discriminatorValue: String,
    val targetType: KSType
)
