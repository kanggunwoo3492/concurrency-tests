package com.hebeyond.arllimtester.concurrency

import javax.sql.DataSource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class SingleDatabaseTest {

    @Test
    fun testSingleDatabase() {
        val dataSource = getDataSource()
        assertDoesNotThrow {
            resetDatabase(dataSource)
        }
    }
    private fun resetDatabase(dataSource: DataSource) {
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(
                    //language=PostgreSQL
                    """
                        UPDATE article 
                        SET read_count = 0, change_happened = false;
                        """
                )
            }
        }
    }
}
