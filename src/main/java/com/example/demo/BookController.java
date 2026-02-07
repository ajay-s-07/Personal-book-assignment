package com.example.demo;

import com.example.demo.db.Book;
import com.example.demo.db.BookRepository;
import com.example.demo.google.GoogleBook;
import com.example.demo.google.GoogleBookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController
public class BookController {
    private final BookRepository bookRepository;
    private final GoogleBookService googleBookService;

    @Autowired
    public BookController(BookRepository bookRepository, GoogleBookService googleBookService) {
        this.bookRepository = bookRepository;
        this.googleBookService = googleBookService;
    }

    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("/google")
    public GoogleBook searchGoogleBooks(@RequestParam("q") String query,
                                        @RequestParam(value = "maxResults", required = false) Integer maxResults,
                                        @RequestParam(value = "startIndex", required = false) Integer startIndex) {
        return googleBookService.searchBooks(query, maxResults, startIndex);
    }

    @PostMapping("/books/{googleId}")
    public ResponseEntity<Book> addBookFromGoogle(@PathVariable String googleId) {
        GoogleBook.Item item = googleBookService.getBookById(googleId);
        
        if (item == null || item.volumeInfo() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        GoogleBook.VolumeInfo info = item.volumeInfo();
        String firstAuthor = (info.authors() != null && !info.authors().isEmpty()) 
                ? info.authors().get(0) 
                : null;
        
        Book book = new Book(item.id(), info.title(), firstAuthor, info.pageCount());
        Book saved = bookRepository.save(book);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
