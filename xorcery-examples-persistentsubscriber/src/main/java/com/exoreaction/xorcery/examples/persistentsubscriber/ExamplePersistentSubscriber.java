package com.exoreaction.xorcery.examples.persistentsubscriber;

import dev.xorcery.domainevents.api.DomainEvent;
import dev.xorcery.domainevents.api.JsonDomainEvent;
import dev.xorcery.domainevents.api.MetadataEvents;
import dev.xorcery.neo4jprojections.spi.Neo4jEventProjection;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;
import org.neo4j.graphdb.Transaction;

/**
 * Example Neo4j Event Projection (replaces PersistentSubscriber)
 *
 * This example demonstrates how to use the Neo4j projections API in Xorcery 0.166.9+
 * which replaces the old persistent subscriber functionality.
 *
 * The projection:
 * 1. Receives domain events through the MetadataEvents parameter
 * 2. Filters for events from the "CreateApplication" command
 * 3. Writes them to Neo4j using the provided transaction
 */
@Service(name = "examplesubscriber")
public class ExamplePersistentSubscriber implements Neo4jEventProjection {

    private static final Logger logger = LogManager.getLogger(ExamplePersistentSubscriber.class);

    public ExamplePersistentSubscriber() {
        logger.info("ExamplePersistentSubscriber initialized as Neo4j projection");
    }

    @Override
    public void write(MetadataEvents events, Transaction transaction) throws Throwable {
        // Filter and handle events from CreateApplication command
        events.data().stream()
                .filter(this::isCreateApplicationEvent)
                .forEach(event -> handleEvent(event, transaction));
    }

    /**
     * Filter: Skip all events that are not from the command CreateApplication
     */
    private boolean isCreateApplicationEvent(DomainEvent event) {
        // Convert DomainEvent to JsonDomainEvent if needed
        if (event instanceof JsonDomainEvent jde) {
            JsonNode json = jde.json();
            JsonNode commandType = json.get("commandType");
            if (commandType != null && "CreateApplication".equals(commandType.asText())) {
                logger.info("Filtered CreateApplication event: {}", json);
                return true;
            }
        }
        return false;
    }

    /**
     * Handle the CreateApplication event by writing to Neo4j
     */
    private void handleEvent(DomainEvent event, Transaction transaction) {
        if (!(event instanceof JsonDomainEvent jde)) {
            logger.warn("Event is not a JsonDomainEvent, skipping");
            return;
        }

        try {
            JsonNode json = jde.json();
            logger.info("Handling CreateApplication event: {}", json.toPrettyString());

            // Example: Extract data from event and write to Neo4j
            JsonNode applicationId = json.get("applicationId");
            JsonNode applicationName = json.get("applicationName");

            if (applicationId != null && applicationName != null) {
                String cypher = """
                    MERGE (app:Application {id: $id})
                    SET app.name = $name,
                        app.createdAt = timestamp()
                    """;

                transaction.execute(cypher,
                        java.util.Map.of(
                                "id", applicationId.asText(),
                                "name", applicationName.asText()
                        ));

                logger.info("Created Application node in Neo4j: {} - {}",
                        applicationId.asText(), applicationName.asText());
            } else {
                logger.warn("Event missing required fields: applicationId or applicationName");
            }
        } catch (Exception e) {
            logger.error("Error handling CreateApplication event", e);
            throw e;
        }
    }
}