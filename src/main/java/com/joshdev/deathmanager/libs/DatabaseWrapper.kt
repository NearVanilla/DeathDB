/* Licensed under GNU General Public License v3.0 */
package com.joshdev.deathmanager.libs

import com.joshdev.deathmanager.exceptions.DeathManagerException
import org.bukkit.entity.Player
import java.sql.DriverManager
import java.sql.ResultSet
import java.time.Instant

class DatabaseWrapper(dbPath: String) {
    private val databaseConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath") ?: throw DeathManagerException("Failed to establish connection to database.")

    fun createDeathsTable() {
        try {
            val createTableStmt =
                databaseConnection.prepareStatement("CREATE TABLE IF NOT EXISTS deaths(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, uniqueId TEXT NOT NULL, timeOfDeath INTEGER NOT NULL, posX REAL NOT NULL, posY REAL NOT NULL, posZ REAL NOT NULL, worldName TEXT NOT NULL, serializedInventory TEXT NOT NULL)")
                    ?: throw DeathManagerException("Failed to prepare the create table statement.")
            createTableStmt.execute()
        } catch (e: Exception) {
        }
    }

    fun addDeathRecord(playerWhoDied: Player) {
        val addDeathStmt = databaseConnection.prepareStatement("INSERT INTO deaths VALUES(NULL, ?, ?, ?, ?, ?, ?, ?)") ?: throw DeathManagerException("Failed to prepare the add death statement.")
        addDeathStmt.setString(1, playerWhoDied.uniqueId.toString())
        addDeathStmt.setLong(2, Instant.now().epochSecond)
        addDeathStmt.setDouble(3, playerWhoDied.location.x)
        addDeathStmt.setDouble(4, playerWhoDied.location.y)
        addDeathStmt.setDouble(5, playerWhoDied.location.z)
        addDeathStmt.setString(6, playerWhoDied.world.name)
        addDeathStmt.setString(7, Serialization.Serialize(playerWhoDied.inventory.contents))
        addDeathStmt.execute()
    }

    fun getPlayerInformation(player: Player): ResultSet {
        val getPlayerInfoStmt = databaseConnection.prepareStatement("SELECT * FROM deaths WHERE uniqueId = ? ORDER BY timeOfDeath DESC LIMIT 5") ?: throw DeathManagerException("Failed to prepare the get player info statement.")
        getPlayerInfoStmt.setString(1, player.uniqueId.toString())
        return getPlayerInfoStmt.executeQuery()
    }
}
