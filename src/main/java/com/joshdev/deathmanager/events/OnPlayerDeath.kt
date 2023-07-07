package com.joshdev.deathmanager.events

import com.joshdev.deathmanager.DeathManager
import com.joshdev.deathmanager.libs.Serialization
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import java.time.Instant

class OnPlayerDeath : Listener{
    @EventHandler
    fun onPlayerDeath(event : PlayerDeathEvent){
        val player = event.player
        val connection = DeathManager.dbConnection
        val stmt = connection.createStatement()
        val serializedInventory = Serialization.Serialize(player.inventory)
        stmt.execute("INSERT INTO deaths VALUES(NULL, '${player.uniqueId}', ${Instant.now().epochSecond}, ${player.location.x}, ${player.location.y}, ${player.location.z}, '${player.world.name}', '${serializedInventory}')")
        val successComponent = Component.text("Your death has been saved.", NamedTextColor.GREEN, TextDecoration.BOLD)
        player.sendMessage(successComponent)
    }
}