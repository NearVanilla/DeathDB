package near.vanilla.death.database

import org.bukkit.Bukkit

fun warn(message : String, throwable : Throwable) =
        Bukkit.getLogger().warning("${PluginLifecycle.TAG}: $message - ${throwable.message}")

fun warn(message : String) =
        Bukkit.getLogger().warning("${PluginLifecycle.TAG}: $message")

fun info(message : String) =
        Bukkit.getLogger().info("${PluginLifecycle.TAG}: $message")

