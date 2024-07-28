package com.exoreaction.xorcery.examples.persistentsubscriber;

import com.exoreaction.xorcery.reactivestreams.api.MetadataJsonNode;
import com.exoreaction.xorcery.reactivestreams.persistentsubscriber.providers.BasePersistentSubscriber;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.jvnet.hk2.annotations.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@Service(name="examplesubscriber")
public class ExamplePersistentSubscriber
        extends BasePersistentSubscriber {

    /**
     * Skip all events that are not from the command CreateApplication, plus any skipOld,skipUntil rules from the base subscriber impl.
     */
    @Override
    public Predicate<MetadataJsonNode<ArrayNode>> getFilter() {
        return super.getFilter().and(wman ->
                wman.metadata().getString("commandType").map(s -> s.equals("CreateApplication")).orElse(false));
    }

    @Override
    public void handle(MetadataJsonNode<ArrayNode> eventsWithMetadata, CompletableFuture<Void> result) {
        System.out.println("Handled " + eventsWithMetadata);
        result.complete(null);
    }
}
