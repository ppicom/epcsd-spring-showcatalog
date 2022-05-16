package edu.uoc.epcsd.showcatalog.controllers;

import edu.uoc.epcsd.showcatalog.controllers.dtos.CategoryDto;
import edu.uoc.epcsd.showcatalog.entities.Category;
import edu.uoc.epcsd.showcatalog.repositories.CategoryRepository;
import edu.uoc.epcsd.showcatalog.services.CatalogService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CatalogService catalogService;

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public List<Category> getAllCategories() {
        log.trace("getAllCategories");

        return catalogService.getAllCategories();
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public void createCategory(@RequestBody CategoryDto category) {
        log.trace("Create category " + category.name);
        catalogService.createCategory(category.name, category.description);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCategory(@PathVariable long id) {
        catalogService.deleteCategory(id);
    }

}
