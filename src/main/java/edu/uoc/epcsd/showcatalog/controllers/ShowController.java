package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.controllers.dtos.PerformanceDto;
import edu.uoc.epcsd.showcatalog.controllers.dtos.ShowDto;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import edu.uoc.epcsd.showcatalog.services.CatalogService;
import edu.uoc.epcsd.showcatalog.services.exceptions.CategoryNotFoundException;
import edu.uoc.epcsd.showcatalog.services.exceptions.ShowNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/show")
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
    // add the code for the missing system operations here
}
