package com.example.helloarkotlin

import org.ini4j.IniPreferences
import org.ini4j.Ini
import java.io.File


object IniHelper {
    fun readIni(path: String): IniPreferences = IniPreferences(Ini(File("../../../config/$path")))
}