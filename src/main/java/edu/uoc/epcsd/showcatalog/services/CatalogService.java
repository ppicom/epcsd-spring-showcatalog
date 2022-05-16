package edu.uoc.epcsd.showcatalog.services;

import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.entities.Performance;
import edu.uoc.epcsd.showcatalog.entities.Show;
import edu.uoc.epcsd.showcatalog.entities.Status;
import edu.uoc.epcsd.showcatalog.repositories.CategoryRepository;
import edu.uoc.epcsd.showcatalog.repositories.PerformanceRepository;
import edu.uoc.epcsd.showcatalog.repositories.ShowRepository;
import edu.uoc.epcsd.showcatalog.services.exceptions.CategoryNotFoundException;
import edu.uoc.epcsd.showcatalog.services.exceptions.ShowNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CatalogService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private PerformanceRepository performanceRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    public List<Show> getAllShows() { return showRepository.findAll(); }

    public Long createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        categoryRepository.save(category);
        return category.getId();
    }

    public Show createShow(Long categoryId, String name, String description, String image, double price,
                           int duration, int capacity) throws CategoryNotFoundException {
        Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);

        Show show = new Show();

        show.setName(name);
        show.setDescription(description);
        show.setImage(image);
        show.setPrice(price);
        show.setDuration(duration);
        show.setCapacity(capacity);
        show.setStatus(Status.CREATED);

        List<Category> currentCategories = new ArrayList<>();
        currentCategories.add(category);
        show.setCategories(currentCategories);

        showRepository.save(show);

        return show;
    }

    public Long createPerformance(Long showId,
                             String name,
                             String description,
                             String image,
                             double price,
                             int capacity,
                             int duration) throws ShowNotFoundException {
        Show show = showRepository.findById(showId).orElseThrow(ShowNotFoundException::new);

        Performance performance = new Performance();
        performance.setName(name);
        performance.setDescription(description);
        performance.setImage(image);
        performance.setPrice(price);
        performance.setCapacity(capacity);
        performance.setDuration(duration);
        performance.setShow(show);
        performance.setStatus(Status.CREATED);
        performance.setRemainingSeats(capacity);
        performance.setStreamingURL("streaming.com/" + showId);

        performanceRepository.save(performance);

        return performance.getId();
    }

    public void deleteCategory(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    public void deleteShow(Long showId) {
        showRepository.deleteById(showId);
    }

    public void deletePerformance(Long showId, Long performanceId) {
        showRepository.findById(showId).ifPresentOrElse((show) -> {
           List<Performance> filteredPerformances = show.getPerformances().stream()
                    .filter((performance) -> !performance.getId().equals(performanceId))
                    .collect(Collectors.toList());

           show.setPerformances(filteredPerformances);

           showRepository.save(show);
        }, ShowNotFoundException::new);
    }

    public List<Show> listShowsByName(String name) {
        return showRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Show> listShowsByCategory(Long categoryId) {
       List<Show> shows = categoryRepository.findById(categoryId)
               .map(Category::getShows)
               .orElse(Collections.emptyList());

       return shows;
    }

    public Optional<Show> viewShow(Long showId) {
        return showRepository.findById(showId);
    }

    public Optional<List<Performance>> listPerformancesOfShow(Long showId) {
        return showRepository.findById(showId).map(Show::getPerformances);
    }
}
