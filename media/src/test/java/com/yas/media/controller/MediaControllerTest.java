package com.yas.media.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.service.MediaService;
import com.yas.media.viewmodel.MediaPostVm;
import com.yas.media.viewmodel.MediaVm;
import com.yas.media.viewmodel.NoFileMediaVm;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class MediaControllerTest {

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private MediaController mediaController;

    private Media media;
    private MediaVm mediaVm;

    @BeforeEach
    void setUp() {
        media = new Media();
        media.setId(1L);
        media.setCaption("Test caption");
        media.setFileName("test.png");
        media.setMediaType(MediaType.IMAGE_PNG_VALUE);

        // Required mock mapping fields
        mediaVm = new MediaVm(1L, "Test caption", "test.png", MediaType.IMAGE_PNG_VALUE, "url");
    }

    @Test
    void create_ShouldReturnOk() {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", MediaType.IMAGE_PNG_VALUE,
                "test".getBytes());
        MediaPostVm postVm = new MediaPostVm("Test caption", file, "test.png");
        when(mediaService.saveMedia(any())).thenReturn(media);

        ResponseEntity<Object> response = mediaController.create(postVm);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        NoFileMediaVm body = (NoFileMediaVm) response.getBody();
        assertEquals(1L, body.id());
    }

    @Test
    void delete_ShouldReturnNoContent() {
        doNothing().when(mediaService).removeMedia(1L);

        ResponseEntity<Void> response = mediaController.delete(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void get_WhenExists_ShouldReturnMedia() {
        when(mediaService.getMediaById(1L)).thenReturn(mediaVm);

        ResponseEntity<MediaVm> response = mediaController.get(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mediaVm, response.getBody());
    }

    @Test
    void get_WhenNotFound_ShouldReturn404() {
        when(mediaService.getMediaById(1L)).thenReturn(null);

        ResponseEntity<MediaVm> response = mediaController.get(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getByIds_WhenExists_ShouldReturnList() {
        when(mediaService.getMediaByIds(List.of(1L))).thenReturn(List.of(mediaVm));

        ResponseEntity<List<MediaVm>> response = mediaController.getByIds(List.of(1L));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getByIds_WhenEmpty_ShouldReturn404() {
        when(mediaService.getMediaByIds(List.of(1L))).thenReturn(Collections.emptyList());

        ResponseEntity<List<MediaVm>> response = mediaController.getByIds(List.of(1L));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getFile_ShouldReturnInputStream() {
        InputStream is = new ByteArrayInputStream("test".getBytes());
        MediaDto mediaDto = MediaDto.builder()
                .content(is)
                .mediaType(MediaType.IMAGE_PNG)
                .build();
        when(mediaService.getFile(1L, "test.png")).thenReturn(mediaDto);

        ResponseEntity<InputStreamResource> response = mediaController.getFile(1L, "test.png");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
    }
}
