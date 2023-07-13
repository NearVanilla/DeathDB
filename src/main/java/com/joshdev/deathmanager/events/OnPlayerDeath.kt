/* Licensed under GNU General Public License v3.0 */
package com.joshdev.deathmanager.events

import com.joshdev.deathmanager.DeathManager
import com.joshdev.deathmanager.libs.Serialization
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import java.lang.Exception
import java.time.Instant

class OnPlayerDeath : Listener {
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        val connection = DeathManager.dbConnection
        val serializedInventory = Serialization.Serialize(player.inventory)
        try {
            val stmt = connection.prepareStatement("INSERT INTO deaths VALUES(NULL, ?, ?, ?, ?, ?, ?, ?)")
            stmt.setString(1, player.uniqueId.toString())
            stmt.setInt(2, Instant.now().epochSecond.toInt())
            stmt.setDouble(3, player.location.x)
            stmt.setDouble(4, player.location.y)
            stmt.setDouble(5, player.location.z)
            stmt.setString(6, player.world.name)
            stmt.setString(7, serializedInventory)
            stmt.execute()
        } catch (e: Exception) { // Could be SQLException but execute can also throw a timeout exception.
            val failedInsertComponent = Component.text(
                "An error was encountered while attempting to save your death to the database. This death has not ben saved.",
                NamedTextColor.RED,
                TextDecoration.BOLD,
            )
            player.sendMessage(failedInsertComponent)
            DeathManager.pluginLogger.warning("Error occurred on player death.\n${e.message}")
        }
        val successComponent = Component.text("Your death has been saved.", NamedTextColor.GREEN, TextDecoration.BOLD)
        player.sendMessage(successComponent)
    }
}
