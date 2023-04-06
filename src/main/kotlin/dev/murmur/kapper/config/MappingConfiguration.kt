package dev.murmur.kapper.config

data class MappingConfiguration(
    val target: String,
    val source: String = "",
    val ignore: Boolean = false,
    val defaultValue: String = "",
)
