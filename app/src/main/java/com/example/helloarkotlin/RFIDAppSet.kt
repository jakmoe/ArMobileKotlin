package com.example.helloarkotlin
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class RFIDAppSet(val a: Int, val b: String = "42")