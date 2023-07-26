package cz.vlasec.countryrouting;

import cz.vlasec.countryrouting.util.BorderUtils;
import cz.vlasec.countryrouting.util.SingleList;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.*;

/**
 * <p>Routing service keeps data needed for routing and finds optimal routes for provided origin and destination.</p>
 * <p>Border data are retrieved from external source. This data may be kept in-memory to optimize performance.</p>
 */
@Service
public class RoutingService {
    private final Map<String, List<String>> borders;
    private final Map<String, Set<String>> islands;
    private final Map<RouteSpec, List<String>> simpleCache;

    public RoutingService(RestTemplateBuilder restBuilder) {
        borders = BorderUtils.getBorders(restBuilder);
        islands = BorderUtils.getIslands(borders);
        simpleCache = new ConcurrentHashMap<>();
    }

    /**
     * @return a non-empty list for countries where routing is possible, empty list otherwise
     */
    public List<String> getRouting(String origin, String destination) {

        // Since algorithm efficiency mentioned explicitly in task description:
        //
        // Breadth-first search should be fast and simple for a few hundred countries.
        // Additionally, the service keeps track of islands (and continent clusters) for faster rejections.
        //
        // Double-sided BFS would be also a possibility, meeting in the middle. I chose not to optimize that much.
        // It would result in much worse readability and the performance gains would be minimal in this graph.
        // With no fitness function or weights, there is no use for fancier algorithms like Dijkstra or A*.
        //
        // Considering that there are only a few hundred countries, a simple cache can be used as N^3 is no big deal.
        // However, caching only within islands is useful to prevent cache from overflowing with bogus inputs.

        if (islands.getOrDefault(origin, emptySet()).contains(destination)) {
            return simpleCache.computeIfAbsent(
                    new RouteSpec(origin, destination),
                    spec -> computeRouting(origin, destination)
            );
        }
        return Collections.emptyList();
    }

    /** Computes routing, throwing runtime exception if route is unavailable. Checks for islands are not performed. */
    private List<String> computeRouting(String origin, String destination) {
        var queue = new LinkedList<>(singletonList(SingleList.<String>nil().prepend(origin)));
        var explored = new HashSet<>(singleton(origin));
        while (!queue.isEmpty()) {
            var path = queue.poll();
            if (destination.equals(path.head)) {
                return path.toList();
            }
            var nextCountries = borders.getOrDefault(path.head, emptyList());
            nextCountries.stream()
                    .filter(c -> !explored.contains(c))
                    .forEach(c -> {
                        queue.add(path.prepend(c));
                        explored.add(c);
                    });
        }
        throw new IllegalStateException("No route found for two countries on the same island");
    }

    private record RouteSpec(String origin, String destination) { }
}
