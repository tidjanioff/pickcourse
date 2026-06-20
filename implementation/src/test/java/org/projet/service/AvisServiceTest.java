package org.projet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projet.repository.CoursRepository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AvisServiceTest {

    @BeforeEach
    void setUp() throws Exception {
        CoursRepository mockRepo = mock(CoursRepository.class);
        when(mockRepo.getAllCoursesId()).thenReturn(Optional.of(List.of("IFT2255")));

        Field instanceField = CoursService.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        CoursService coursService = CoursService.getInstance();
        coursService.setCoursRepository(mockRepo);
    }

    @Test
    void enregistrerAvisPropageLesErreursDeValidation() {
        AvisService service = AvisService.getInstance();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.enregistrerAvis("IFT9999", "Prof Test", 3, 4, "Commentaire")
        );
    }
}
