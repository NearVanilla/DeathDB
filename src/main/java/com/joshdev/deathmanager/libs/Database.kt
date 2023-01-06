package com.joshdev.deathmanager.libs

import com.joshdev.deathmanager.DeathManager
import org.bukkit.Location
import org.bukkit.inventory.PlayerInventory
import java.sql.ResultSet
import java.util.*

class Database {

    companion object{

        fun SetupDatabaseIfNotExist(id: UUID){

            val formattedID = id.toString().replace("-", "")
            val connection = DeathManager.dbConnection
            val stmt = connection?.createStatement()
            val query = "CREATE TABLE IF NOT EXISTS $formattedID(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, pos_x TEXT, pos_y TEXT, pos_z TEXT, world TEXT, inv TEXT);"
            stmt?.execute(query)

        }

        fun AddRecord(id: UUID, position: Location, world: String, inv: PlayerInventory){

            val formattedID = id.toString().replace("-", "")
            val serializedInventory = Serialization.Serialize(inv)

            val connection = DeathManager.dbConnection
            val stmt = connection?.createStatement()

            val query = "INSERT INTO $formattedID(id, pos_x, pos_y, pos_z, world, inv) VALUES(NULL, '${position.x}', '${position.y}', '${position.z}', '$world', '$serializedInventory');"

            stmt?.execute(query)

        }

        fun GetRecords(id: UUID): ResultSet? {

            val formattedID = id.toString().replace("-", "")

            val connection = DeathManager.dbConnection
            val stmt = connection?.createStatement()

            val query = "SELECT * FROM $formattedID LIMIT 5"

            return stmt?.executeQuery(query)

        }

    }

}