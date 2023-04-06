package dev.murmur.kapper.config

import kotlin.reflect.KClass

annotation class TargetType(
    val discriminatorValue: String,
    val targetType: KClass<*>
)

