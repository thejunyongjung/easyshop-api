package org.yearup.service;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import org.yearup.models.Category;
import org.yearup.repository.CategoryRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@Sql(scripts = "classpath:test-insert-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class CategoryServiceTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    public void getAllCategories_shouldReturn_allCategories()
    {
        // arrange
        CategoryService categoryService = new CategoryService(categoryRepository);

        // act
        int categoryCount = categoryService.getAllCategories().size();

        // assert
        assertEquals(3, categoryCount, "Because the test data has 3 categories.");
    }

    @Test
    public void getById_shouldReturn_nullWhenCategoryDoesNotExist()
    {
        // arrange
        CategoryService categoryService = new CategoryService(categoryRepository);

        // act
        Category category = categoryService.getById(622);

        // assert
        assertNull(category, "Because there's no category with id 622");
    }

    @Test
    public void create_shouldAdd_aNewCategory()
    {
        // arrange
        CategoryService categoryService = new CategoryService(categoryRepository);

        // act
        Category saved = categoryService.create(new Category(0, "Galaxy Z TriFold",
                "Samsung's revolutionary dual-hinge smartphone that unfolds twice to reveal a massive 10.0-inch panoramic workspace"));

        // assert
        assertNotNull(saved, "Because create should return the saved category");
        assertEquals(4, categoryService.getAllCategories().size(),
                "Because creating one category should add to the 3 exiting ones.");
    }

    @Test
    public void delete_shouldRemove_theCategory()
    {
        // arrange
        CategoryService categoryService = new CategoryService(categoryRepository);
        int id = categoryRepository.findAll().get(0).getCategoryId();

        // act
        categoryService.delete(id);

        // assert
        assertEquals(2, categoryService.getAllCategories().size(),
                "Because there should be 2 categories remaining after deleting one of the 3");
    }
}
