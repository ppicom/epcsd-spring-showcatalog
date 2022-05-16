package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.controllers.dtos.PerformanceDto;
import edu.uoc.epcsd.showcatalog.controllers.dtos.ShowDto;
import edu.uoc.epcsd.showcatalog.entities.Performance;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.kafka.KafkaConstants;
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

import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/shows")
public class ShowController {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private KafkaTemplate<String, Show> kafkaTemplate;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public Long createShow(@RequestBody ShowDto showDto) throws CategoryNotFoundException {
        log.trace("Create show");

        Show show = catalogService.createShow(showDto.categoryId,
                showDto.name,
                showDto.description,
                showDto.image,
                showDto.price,
                showDto.duration,
                showDto.capacity);

        log.info("Sending message to topic " + KafkaConstants.SHOW_TOPIC + " after creating show " + show.getId() + "...");
        kafkaTemplate.send(KafkaConstants.SHOW_TOPIC, show);
        log.info("Message sent.");

        return show.getId();
    }

    @PostMapping("/{showId}/performances")
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
    @ResponseBody
    public List<Show> getShows(@QueryParam(value = "name") String name,
                               @QueryParam(value = "categoryId") String categoryId) throws CategoryNotFoundException {
        if (name != null) {
            return catalogService.listShowsByName(name);
        } else if (categoryId != null) {
            return catalogService.listShowsByCategory(Long.parseLong(categoryId));
        } else {
            return catalogService.getAllShows();
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

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void cancelShow(@PathVariable long id) throws ShowNotFoundException {
        this.catalogService.cancelShow(id);
    }

    @ExceptionHandler({CategoryNotFoundException.class, ShowNotFoundException.class})
    public ResponseEntity<Void> handleException() {
        return ResponseEntity.notFound().build();
    }
}
