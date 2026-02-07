package com.example.demo.google;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GoogleBookService {
    private final RestClient restClient;

    public GoogleBookService(@Value("${google.books.base-url:https://www.googleapis.com/books/v1}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public GoogleBook searchBooks(String query, Integer maxResults, Integer startIndex) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/volumes")
                        .queryParam("q", query)
                        .queryParam("maxResults", maxResults != null ? maxResults : 10)
                        .queryParam("startIndex", startIndex != null ? startIndex : 0)
                        .build())
                .retrieve()
                .body(GoogleBook.class);
    }

//  To fetch single book volume by its Google Books ID.
    public GoogleBook.Item getBookById(String volumeId) {
        try {
            return restClient.get()
                    .uri("/volumes/{volumeId}", volumeId)
                    .retrieve()
                    .body(GoogleBook.Item.class);
        }
        // invalid id
        catch (org.springframework.web.client.HttpClientErrorException.NotFound ex) {
            return null;
        }
        // other google api errors
        catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests ex) {
            throw new org.springframework.web.server.ResponseStatusException(429, "Google API quota exceeded", ex);
        } catch (org.springframework.web.client.RestClientResponseException ex) {
            throw new org.springframework.web.server.ResponseStatusException(ex.getStatusCode(), "Google API error", ex);
        }
    }
}

