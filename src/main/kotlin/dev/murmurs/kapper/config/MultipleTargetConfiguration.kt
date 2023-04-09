package dev.murmurs.kapper.config

import com.google.devtools.ksp.symbol.KSType

data class MultipleTargetConfiguration(
    val discriminator: String,
    val discriminatorType: KSType,
    val targetTypes: List<TargetTypeConfiguration>,
)
