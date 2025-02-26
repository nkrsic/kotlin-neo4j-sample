package org.example

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Record
import org.neo4j.driver.Result
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

fun printPeople(initial: String, driver: Driver){
    driver.session().use { session ->
        // A Managed transaction is a quick and easy way to wrap a Cypher Query.
        // The `session.run` method will run the specified Query.
        // This simpler method does not use any automatic retry mechanism.
        val result: Result = session.run(
            "MATCH (a:Person) WHERE a.name STARTS WITH \$x RETURN a.name AS name",
            parameters("x", initial)
        )
        // Each Cypher execution returns a stream of records.
        while (result.hasNext()) {
            val record: Record = result.next()
            // Values can be extracted from a record by index or name.
            System.out.println(record.get("name").asString())
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

    printPeople("A", driver)

    driver.close()
}