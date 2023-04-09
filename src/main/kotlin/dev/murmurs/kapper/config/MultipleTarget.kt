package dev.murmurs.kapper.config

annotation class MultipleTarget(
    val discriminator: String,
    val targetTypes: Array<TargetType>
)

