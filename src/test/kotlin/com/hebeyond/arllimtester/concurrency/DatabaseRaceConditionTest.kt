package com.hebeyond.arllimtester.concurrency

import jakarta.persistence.LockModeType
import java.lang.Thread.sleep
import javax.sql.DataSource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.data.jpa.repository.Lock

class DatabaseRaceConditionTest {

    @AfterEach
    fun afterEach() {
        val dataSource = getDataSource()
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    //language=PostgreSQL
                    """
                    UPDATE article SET change_happened = false, read_count = 0;
                    """
                )
            }
        }
    }


    @Test
    fun databaseRaceConditionTest() {
        raceCondition()
    }

    private fun raceCondition(): Boolean {
        val dataSources = (1..5).map { getDataSource() }

        // give threads the dataSources
        val threads = dataSources.map {
            Thread {
                val ids = getIds(it)

                println("ids size: ${ids.size}")

                if (ids.isEmpty()) {
                    return@Thread
                }
                updateReadCount(it, ids)
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }
        return true
    }

    private fun updateReadCount(it: DataSource, ids: List<Long>) {
        for (id in ids) {
            it.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeUpdate(
                        //language=PostgreSQL
                        """
                        UPDATE article 
                        SET read_count = read_count + 1, change_happened = true
                        WHERE id = $id AND change_happened = false;
                        """
                    )
                }
            }
        }
    }
    private fun getIds(it: DataSource): List<Long> {
        val ids = mutableListOf<Long>()
        it.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(
                    //language=PostgreSQL
                    """
                    SELECT id FROM article;
                    """
                ).use { resultSet ->
                    if (resultSet.next()) {
                        ids.add(resultSet.getLong("id"))
                    }
                }
            }
        }
        sleep(1000)
        return ids
    }
}
