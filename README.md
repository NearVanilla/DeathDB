# DeathDB

## About

DeathDB is a plugin that allows Staff Members to log data about Player Deaths.

When a player dies, DeathDB will log the following information to a Table with the name of
their UUID in an SQLite Database:

- The location (X, Y, Z) of the death.
- The name of the world the player died in.
- A serialized copy of the players inventory.

This information can then be retrieved at a later date by Staff Members. They also have the
option of restoring the players inventory, if they deem necessary.

## Compilation

If you wish to compile DeathDB yourself, please follow the steps below:

- Clone the repository into a directory of your choice through running `git clone https://github.com/105hua/DeathDB.git`
- Navigate to the directory you cloned the repository into.
- In a terminal, run `gradlew build` on Windows or `./gradlew build` on Linux.
- The compiled jar file will be located in `build/libs/DeathDB-<version>.jar`, upon completion.
- You can then move this jar file into your plugins folder.

## Usage

DeathDB provides two commands for Staff Members to use:

- `/showdeaths <player>`: Using this command on a valid player will display the last 5 available deaths in your chat.
- `/restoreinventory <player> <index>`: Using this command on a valid player, alongside an index taken from the previous command will restore the players inventory to the state it was in before the death occurred.

To run the `showdeaths` command, you will need the `deathdb.showdeaths` permission. For the
`restoreinventory` command, you will need the `deathdb.restoreinventory` permission.