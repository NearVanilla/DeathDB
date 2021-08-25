package com.nearvanilla.death.database

import java.io.File

object DeathDatabaseFileSystem {

    private val ROOT_STORE = getOrCreateDirectory(PluginLifecycle.INSTANCE.dataFolder)

    operator fun get(fileName : String) : File = File(ROOT_STORE, fileName)

    private fun getOrCreateDirectory(file : File) : File {
        file.mkdirs()
        return file
    }

}