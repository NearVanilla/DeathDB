package com.nearvanilla.death.database

import org.bukkit.Bukkit

fun warn(message : String, throwable : Throwable) =
        PluginLifecycle.INSTANCE.getLogger().warning("${PluginLifecycle.TAG}: $message - ${throwable.message}")

fun warn(message : String) =
        PluginLifecycle.INSTANCE.getLogger().warning("${PluginLifecycle.TAG}: $message")

fun info(message : String) =
        PluginLifecycle.INSTANCE.getLogger().info("${PluginLifecycle.TAG}: $message")

