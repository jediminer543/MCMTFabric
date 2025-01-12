package net.himeki.mcmtfabric.parallelised;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConcurrentCollections {

    private static final Logger LOGGER = LogManager.getLogger();

    public static <T> Set<T> newHashSet() {
        LOGGER.info("Concurrent hash set created");
        return Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());
    }

    public static <T> List<T> newLinkedList() {
        LOGGER.info("Concurrent \"linked\" list created");
        return new CopyOnWriteArrayList<T>();
    }

    public static <T> Collector<T, ?, List<T>> toList() {
        return Collectors.toCollection(CopyOnWriteArrayList::new);
    }
}
