package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.controllers.dtos.PerformanceDto;
import edu.uoc.epcsd.showcatalog.controllers.dtos.ShowDto;
import edu.uoc.epcsd.showcatalog.entities.Performance;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import edu.uoc.epcsd.showcatalog.services.CatalogService;
import edu.uoc.epcsd.showcatalog.services.exceptions.CategoryNotFoundException;
import edu.uoc.epcsd.showcatalog.services.exceptions.ShowNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.glassfish.jersey.server.monitoring.ResponseMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/shows")
public class ShowController {

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private KafkaTemplate<String, Show> kafkaTemplate;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<Show> getAllShows() {
        log.trace("getAllShows");

        return showRepository.findAll();
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> createShow(@RequestBody ShowDto show) {
        try {
            log.trace("Create show");

            catalogService.createShow(show.categoryId,
                    show.name,
                    show.description,
                    show.image,
                    show.price,
                    show.duration,
                    show.capacity);

            return ResponseEntity.created(URI.create("/show/" + show.name)).build();
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{showId}/performance")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Long> createPerformance(@PathVariable String showId,
                                                  @RequestBody PerformanceDto performanceDto) {
        try {
            this.catalogService.createPerformance(Long.parseLong(showId),
                    performanceDto.name,
                    performanceDto.description,
                    performanceDto.image,
                    performanceDto.price,
                    performanceDto.capacity,
                    performanceDto.duration);
            return ResponseEntity.status(HttpStatus.CREATED).body(Long.parseLong(showId));
        } catch (ShowNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> removeShow(@PathVariable Long id) {
        try {
            this.catalogService.deleteShow(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            log.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{showId}/performance/{performanceId}")
    public ResponseEntity<Void> removePerformance(@PathVariable Long showId, @PathVariable Long performanceId) {
        try {
            this.catalogService.deletePerformance(showId, performanceId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<Show>> getShowsByName(@QueryParam(value = "name") String name) {
        try {
            List<Show> shows = this.catalogService.listShowsByName(name);
            return ResponseEntity.ok(shows);
        } catch (Exception e) {
            log.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<Show>> getShowsByCategory(@QueryParam(value = "categoryId") Long categoryId) {
        try {
            List<Show> shows = this.catalogService.listShowsByCategory(categoryId);
            return ResponseEntity.ok(shows);
        } catch (CategoryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Show getShow(@PathVariable Long id) throws ShowNotFoundException {
        return this.catalogService.viewShow(id).orElseThrow(ShowNotFoundException::new);
    }

    @GetMapping("/{showId}/performances")
    @ResponseBody
    public List<Performance> getPerformancesOfShow(@PathVariable Long showId) throws ShowNotFoundException {
        return this.catalogService.listPerformancesOfShow(showId).orElseThrow(ShowNotFoundException::new);
    }
}
