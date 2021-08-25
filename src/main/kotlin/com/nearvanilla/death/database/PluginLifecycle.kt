package com.nearvanilla.death.database

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class PluginLifecycle : JavaPlugin() {

    override fun onEnable() {
        INSTANCE = this
        Bukkit.getPluginManager().registerEvents(DeathListener, this)
        Bukkit.getPluginManager().registerEvents(DeadInventoryScreen.DeadInventoryCloseListener, this)
        DeathDatabaseCommands.register(this)
    }

    companion object {
        const val TAG = "DeathDatabase"
        lateinit var INSTANCE : PluginLifecycle
    }

}