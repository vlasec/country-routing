package cz.vlasec.countryrouting.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.vlasec.countryrouting.model.CountryData;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.*;

// Utilities that extract borders and calculates islands from provided source data.
// Simple solution chosen to save time as there are no requirements on reloads etc.
public class BorderUtils {
    private BorderUtils() {}

    /**
     * Retrieves borders information from the source JSON. All other information is omitted.
     * Borders are provided as mapping of country code to all its neighbours' country codes.
     */
    public static Map<String, List<String>> getBorders(RestTemplateBuilder restBuilder) {
        var rest = restBuilder.build();
        var mapper = new ObjectMapper();
        var sourceUrl = "https://raw.githubusercontent.com/mledoze/countries/master/countries.json";
        // Retrieve the input JSON
        var json = rest.getForObject(sourceUrl, String.class);
        try {
            // Unmarshall and convert to map. Irrelevant information found in said JSON is ignored.
            return unmodifiableMap(
                    Arrays.stream(mapper.readValue(json, CountryData[].class))
                            .collect(Collectors.toMap(CountryData::cca3, c -> unmodifiableList(c.borders())))
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Using provided borders, this method calculates islands (or continent clusters).
     * This allows an easy check if a land route is even possible.
     */
    public static Map<String, Set<String>> getIslands(Map<String, List<String>> allBorders) {
        var islands = new HashMap<String, Set<String>>();
        allBorders.keySet().forEach(origin -> {
            // Only explore countries that haven't been found while exploring neighbours of a different country.
            if (!islands.containsKey(origin)) {
                var island = new HashSet<String>();
                var queue = new LinkedList<>(singletonList(origin));
                // Explore the new continent until there are no new countries to be reached by land.
                while (!queue.isEmpty()) {
                    var country = queue.poll();
                    island.add(country);
                    var borders = allBorders.get(country);
                    if (borders != null) {
                        // Explore newly found countries bordering with current country from queue, and enqueue them.
                        borders.stream()
                                .filter(c -> !island.contains(c))
                                .forEach(c -> {
                                    island.add(c);
                                    queue.add(c);
                                });
                    }
                }
                var finalIsland = unmodifiableSet(island);
                // Map every country of the island to the entirety of its countries. Put it to the main map.
                island.forEach(country -> islands.put(country, finalIsland));
            }
        });
        return unmodifiableMap(islands);
    }

}
