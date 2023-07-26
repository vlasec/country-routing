package cz.vlasec.countryrouting;

import cz.vlasec.countryrouting.model.RoutingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class RoutingController {
    private final RoutingService service;

    public RoutingController(RoutingService service) {
        this.service = service;
    }

    Logger logger = LoggerFactory.getLogger(RoutingController.class);

    @GetMapping("/routing/{origin}/{destination}")
    public RoutingResponse routing(@PathVariable String origin, @PathVariable String destination) {
        var startTime = System.nanoTime();
        var routing = service.getRouting(origin, destination);
        var timeSpent = System.nanoTime() - startTime;
        logger.info("Routing from {} to {} took {} ns", origin, destination, timeSpent);
        if (routing.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No routing found"
            );
        } else {
            return new RoutingResponse(routing);
        }
    }
}
