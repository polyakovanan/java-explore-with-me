package ru.practicum.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.core.exception.ConditionsNotMetException;
import ru.practicum.core.exception.NotFoundException;
import ru.practicum.core.persistance.model.*;
import ru.practicum.core.persistance.model.dto.compilation.CompilationDto;
import ru.practicum.core.persistance.model.dto.compilation.NewCompilationDto;
import ru.practicum.core.persistance.model.dto.compilation.UpdateCompilationRequest;
import ru.practicum.core.persistance.model.dto.event.state.EventState;
import ru.practicum.core.persistance.repository.CompilationRepository;
import ru.practicum.core.persistance.repository.EventRepository;
import ru.practicum.core.service.impl.CompilationServiceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompilationServiceTest {

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    private Event createTestEvent(Long id) {
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@email.com");

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setAnnotation("Test Annotation");
        event.setDescription("Test Description");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setInitiator(user);
        event.setCategory(category);
        event.setPaid(false);
        event.setParticipantLimit(10L);
        event.setRequestModeration(true);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setLat(55.754167);
        event.setLon(37.620000);
        return event;
    }

    private Compilation createTestCompilation(Long id, String title, boolean pinned, Set<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setId(id);
        compilation.setTitle(title);
        compilation.setPinned(pinned);
        compilation.setEvents(events != null ? events : Collections.emptySet());
        return compilation;
    }

    @Test
    void findAllShouldReturnListOfCompilations() {
        Event event = createTestEvent(1L);
        Compilation compilation = createTestCompilation(1L, "Test Compilation", true, Set.of(event));

        when(compilationRepository.findCompilations(any(), any(), any()))
                .thenReturn(List.of(compilation));

        List<CompilationDto> result = compilationService.findAll(true, 0, 10);

        assertEquals(1, result.size());
        assertEquals("Test Compilation", result.getFirst().getTitle());
        assertTrue(result.getFirst().getPinned());
        assertEquals(1, result.getFirst().getEvents().size());
    }

    @Test
    void findByIdShouldReturnCompilationWhenExists() {
        Event event = createTestEvent(1L);
        Compilation compilation = createTestCompilation(1L, "Test Compilation", false, Set.of(event));

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));

        CompilationDto result = compilationService.findById(1L);

        assertNotNull(result);
        assertEquals("Test Compilation", result.getTitle());
        assertFalse(result.getPinned());
        assertEquals(1, result.getEvents().size());
    }

    @Test
    void findByIdShouldThrowNotFoundExceptionWhenNotExists() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> compilationService.findById(1L));
    }

    @Test
    void createShouldSaveNewCompilation() {
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("New Compilation");
        newCompilationDto.setPinned(true);
        newCompilationDto.setEvents(Set.of(1L, 2L));

        Event event1 = createTestEvent(1L);
        Event event2 = createTestEvent(2L);

        when(compilationRepository.findByTitleIgnoreCase("New Compilation")).thenReturn(Collections.emptyList());
        when(eventRepository.findAllByIdIn(any())).thenReturn(List.of(event1, event2));
        when(compilationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CompilationDto result = compilationService.create(newCompilationDto);

        assertNotNull(result);
        assertEquals("New Compilation", result.getTitle());
        assertTrue(result.getPinned());
        assertEquals(2, result.getEvents().size());
    }

    @Test
    void createShouldThrowExceptionWhenTitleExists() {
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("Existing Compilation");

        Compilation existing = createTestCompilation(1L, "Existing Compilation", false, null);

        when(compilationRepository.findByTitleIgnoreCase("Existing Compilation")).thenReturn(List.of(existing));

        assertThrows(ConditionsNotMetException.class, () -> compilationService.create(newCompilationDto));
    }

    @Test
    void updateShouldUpdateCompilation() {
        Event oldEvent = createTestEvent(1L);
        Event newEvent = createTestEvent(2L);

        Compilation existingCompilation = createTestCompilation(
                1L, "Old Title", false, Set.of(oldEvent));

        UpdateCompilationRequest updateRequest = new UpdateCompilationRequest();
        updateRequest.setTitle("New Title");
        updateRequest.setPinned(true);
        updateRequest.setEvents(Set.of(2L));

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(existingCompilation));
        when(compilationRepository.findByTitleIgnoreCase("New Title")).thenReturn(Collections.emptyList());
        when(eventRepository.findAllByIdIn(any())).thenReturn(List.of(newEvent));
        when(compilationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CompilationDto result = compilationService.update(1L, updateRequest);

        assertEquals("New Title", result.getTitle());
        assertTrue(result.getPinned());
        assertEquals(1, result.getEvents().size());
        assertEquals(1L, result.getEvents().getFirst().getId());
    }

    @Test
    void updateShouldThrowExceptionWhenTitleExists() {
        Compilation existingCompilation = createTestCompilation(1L, "Old Title", false, null);
        Compilation anotherCompilation = createTestCompilation(2L, "Existing Title", true, null);

        UpdateCompilationRequest updateRequest = new UpdateCompilationRequest();
        updateRequest.setTitle("Existing Title");

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(existingCompilation));
        when(compilationRepository.findByTitleIgnoreCase("Existing Title")).thenReturn(List.of(anotherCompilation));

        assertThrows(ConditionsNotMetException.class, () -> compilationService.update(1L, updateRequest));
    }

    @Test
    void updateShouldNotThrowExceptionWhenTitleSame() {
        Compilation existingCompilation = createTestCompilation(1L, "Same Title", false, null);

        UpdateCompilationRequest updateRequest = new UpdateCompilationRequest();
        updateRequest.setTitle("Same Title");

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(existingCompilation));
        when(compilationRepository.findByTitleIgnoreCase("Same Title")).thenReturn(List.of(existingCompilation));
        when(compilationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> compilationService.update(1L, updateRequest));
    }

    @Test
    void deleteShouldDeleteCompilation() {
        Compilation compilation = createTestCompilation(1L, "To Delete", false, null);

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        doNothing().when(compilationRepository).deleteById(1L);

        compilationService.delete(1L);

        verify(compilationRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteShouldThrowNotFoundExceptionWhenNotExists() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> compilationService.delete(1L));
        verify(compilationRepository, never()).deleteById(any());
    }
}