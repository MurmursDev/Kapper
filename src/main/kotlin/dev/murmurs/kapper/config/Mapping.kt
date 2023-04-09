package dev.murmurs.kapper.config

annotation class Mapping(
    val target: String,
    val source: String = "",
    val ignore: Boolean = false,
    val defaultValue: String = "",
)
