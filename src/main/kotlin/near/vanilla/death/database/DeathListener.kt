package near.vanilla.death.database

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

object DeathListener : Listener {

    @EventHandler(priority=EventPriority.MONITOR)
    fun onDeath(deathEvent: PlayerDeathEvent) {
        info("Death event received for player: ${deathEvent.entity.displayName} with uuid: ${(deathEvent.entity as Player).uniqueId.toString()}")
        DeathDatabase[deathEvent.entity] = deathEvent.drops
    }

}