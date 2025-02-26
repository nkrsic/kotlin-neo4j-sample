package org.example

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Transaction
import org.neo4j.driver.Values.parameters


fun addPerson(name: String, driver: Driver): Unit{
    driver.session().use { session ->
        // Wrapping a Cypher Query in a Managed Transaction provides atomicity
        // and makes handling errors much easier.
        // Use `session.writeTransaction` for writes and `session.readTransaction` for reading data.
        // These methods are also able to handle connection problems and transient errors using an automatic retry mechanism.
        session.writeTransaction<Any> { tx: Transaction ->
            val result = tx.run(
                "MERGE (a:Person {name: \$x})",
                parameters("x", name)
            )
        }
    }
}
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {

    val driver: Driver
    val user: String = System.getenv("NEO4J_USER") ?: throw IllegalStateException("NEO4J_USER not set")
    val password: String = System.getenv("NEO4J_PASSWORD") ?: throw IllegalStateException("NEO4J_PASSWORD not set")
    println("Proceeding with user $user and password $password")

    driver = GraphDatabase.driver(
        "bolt://localhost:7687",
        AuthTokens.basic(user, password)
    )

    addPerson("Billy Bob Thornton", driver)
    addPerson("Ada Lovelace", driver)
    addPerson("Alan Turing", driver)

    driver.close()
}