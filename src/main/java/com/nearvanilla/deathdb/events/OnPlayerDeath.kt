/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathdb.events

import com.nearvanilla.deathdb.DeathDB
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import java.lang.Exception

class OnPlayerDeath : Listener {
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        try {
            DeathDB.dbWrapper.addDeathRecord(player)
        } catch (e: Exception) { // Could be SQLException but execute can also throw a timeout exception.
            DeathDB.pluginLogger.warning("Error occurred on player death.\n${e.message}")
        }
    }
}
