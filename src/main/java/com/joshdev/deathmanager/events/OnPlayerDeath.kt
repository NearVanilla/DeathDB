package com.joshdev.deathmanager.events

import com.joshdev.deathmanager.libs.Database
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class OnPlayerDeath : Listener{

    @EventHandler
    fun onPlayerDeath(event : PlayerDeathEvent){

        val player = event.player // Get player from event.
        Database.SetupDatabaseIfNotExist(player.uniqueId)
        Database.AddRecord(player.uniqueId, player.location, player.world.name, player.inventory) // Add info to database.

        val successComponent = Component.text( // Create component for success.

            "Your death was logged.",
            NamedTextColor.GREEN,
            TextDecoration.BOLD

        )

        player.sendMessage(successComponent) // Send player success component.

    }

}