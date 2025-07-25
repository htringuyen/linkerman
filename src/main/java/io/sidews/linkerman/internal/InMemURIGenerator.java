package io.sidews.linkerman.internal;

import org.eclipse.emf.common.util.URI;

import java.util.concurrent.atomic.AtomicLong;

public class InMemURIGenerator {

    private static final AtomicLong generationCounter = new AtomicLong(0);

    static URI createFor(String fileExtension) {
        return URI.createURI("inmem:/content-" + generationCounter.getAndIncrement() + "." + fileExtension);
    }
}
