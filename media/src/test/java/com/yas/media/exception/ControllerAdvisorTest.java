package com.yas.media.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.exception.UnsupportedMediaTypeException;
import com.yas.media.viewmodel.ErrorVm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

@ExtendWith(MockitoExtension.class)
class ControllerAdvisorTest {

    @InjectMocks
    private ControllerAdvisor controllerAdvisor;

    private ServletWebRequest mockWebRequest;
    private HttpServletRequest mockHttpServletRequest;

    @BeforeEach
    void setUp() {
        mockHttpServletRequest = mock(HttpServletRequest.class);
        org.mockito.Mockito.lenient().when(mockHttpServletRequest.getServletPath()).thenReturn("/test/path");
        mockWebRequest = new ServletWebRequest(mockHttpServletRequest);
    }

    @Test
    void handleUnsupportedMediaTypeException_ShouldReturn400() {
        UnsupportedMediaTypeException ex = new UnsupportedMediaTypeException("Not supported");
        ResponseEntity<ErrorVm> response = controllerAdvisor.handleUnsupportedMediaTypeException(ex, mockWebRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("File uploaded media type is not supported", response.getBody().detail());
    }

    @Test
    void handleNotFoundException_ShouldReturn404() {
        NotFoundException ex = new NotFoundException("Not found");
        ResponseEntity<ErrorVm> response = controllerAdvisor.handleNotFoundException(ex, mockWebRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not found", response.getBody().detail());
    }

    @Test
    void handleMethodArgumentNotValid_ShouldReturn400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "message");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleConstraintViolation_ShouldReturn400() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        jakarta.validation.Path path = mock(jakarta.validation.Path.class);
        when(violation.getRootBeanClass()).thenReturn((Class) String.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("error format");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorVm> response = controllerAdvisor.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleRuntimeException_ShouldReturn500() {
        RuntimeException ex = new RuntimeException("Runtime error");
        ResponseEntity<ErrorVm> response = controllerAdvisor.handleIoException(ex, mockWebRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Runtime error", response.getBody().detail());
    }

    @Test
    void handleOtherException_ShouldReturn500() {
        Exception ex = new Exception("General error");
        ResponseEntity<ErrorVm> response = controllerAdvisor.handleOtherException(ex, mockWebRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("General error", response.getBody().detail());
    }
}
