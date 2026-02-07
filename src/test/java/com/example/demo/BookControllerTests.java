package com.example.demo;

import com.example.demo.db.Book;
import com.example.demo.db.BookRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookControllerTests {

    static MockWebServer mockServer;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BookRepository bookRepository;

    @BeforeAll
    static void startServer() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        mockServer.shutdown();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("google.books.base-url", () -> mockServer.url("/").toString());
    }

    @BeforeEach
    void setup() {
        bookRepository.deleteAll();
    }

    @Test
    void testGetAllBooks_returnsPersistedBooks() throws Exception {
        // Seed some test data
        bookRepository.save(new Book("lRtdEAAAQBAJ", "Spring in Action", "Craig Walls"));
        bookRepository.save(new Book("12muzgEACAAJ", "Effective Java", "Joshua Bloch"));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Spring in Action"))
                .andExpect(jsonPath("$[1].title").value("Effective Java"));
    }

    @Test
    void testPostBook_validGoogleId_returns201AndPersistsBook() throws Exception {
        // Enqueue a valid response from mock Google API
        String body = Files.readString(Paths.get("src/test/resources/single_volume.json"));
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        mockMvc.perform(post("/books/ka2VUBqHiWkC"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ka2VUBqHiWkC"))
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.author").value("Joshua Bloch"))
                .andExpect(jsonPath("$.pageCount").value(375));

        // Verify it was persisted
        assertThat(bookRepository.findAll()).hasSize(1);
        Book saved = bookRepository.findAll().get(0);
        assertThat(saved.getId()).isEqualTo("ka2VUBqHiWkC");
        assertThat(saved.getTitle()).isEqualTo("Effective Java");
    }

    @Test
    void testPostBook_invalidGoogleId_returns400AndNothingPersisted() throws Exception {
        // Enqueue a 404 response to simulate book not found
        mockServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .addHeader("Content-Type", "application/json")
                .setBody("{}"));

        mockMvc.perform(post("/books/invalid-id-123"))
                .andExpect(status().isBadRequest());

        // Verify nothing was persisted
        assertThat(bookRepository.findAll()).isEmpty();
    }
}