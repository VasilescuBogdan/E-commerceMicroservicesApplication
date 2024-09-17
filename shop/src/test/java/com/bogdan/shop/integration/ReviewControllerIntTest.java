package com.bogdan.shop.integration;

import com.bogdan.shop.controllers.models.CreateReview;
import com.bogdan.shop.controllers.models.GetProduct;
import com.bogdan.shop.controllers.models.GetReviewDetails;
import com.bogdan.shop.controllers.models.UpdateReview;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.entities.Review;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import com.bogdan.shop.persistence.repositories.ReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerIntTest extends IntTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    private final String baseUrl = "http://localhost:" + port + "/api/reviews";

    private final List<Review> reviews = new ArrayList<>();

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product(null, "product", 10.5F, "description", new ArrayList<>(), new ArrayList<>());
        Review review1 = new Review(null, "user1", "message1", 3, product);
        Review review2 = new Review(null, "user2", "message2", 5, product);
        Review review3 = new Review(null, "user1", "message3", 1, product);
        reviews.addAll(List.of(review1, review2, review3));
        product = productRepository.save(product);
        reviewRepository.saveAll(reviews);
    }

    @AfterEach
    void tearDown() {
        reviewRepository.deleteAll();
        productRepository.deleteAll();
        entityManager.createNativeQuery("ALTER TABLE review AUTO_INCREMENT=1")
                     .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE product AUTO_INCREMENT=1")
                     .executeUpdate();
    }

    @Test
    void addReview_responseStatusCreatedAndCheckNewReviewIsSaved() throws Exception {
        //Arrange
        CreateReview createReview = new CreateReview(product.getId(), 3, "message4");
        String user = "user1";

        //Act
        ResultActions response = mvc.perform(post(baseUrl).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user))
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .content(mapper.writeValueAsString(createReview)));

        //Assert
        response.andExpect(status().isCreated());
        List<Review> updatedReviewList = reviewRepository.findAll();
        assertThat(updatedReviewList).hasSize(reviews.size() + 1);
        Review lastReview = updatedReviewList.get(updatedReviewList.size() - 1);
        assertThat(lastReview.getSender()).isEqualTo(user);
        assertThat(lastReview.getMessage()).isEqualTo(createReview.message());
        assertThat(lastReview.getNumberOfStars()).isEqualTo(createReview.numberOfStars());
        assertThat(lastReview.getProduct()).isEqualTo(product);
    }

    @Test
    void getReviewsSender_responseStatusOkAndReturnReviewList() throws Exception {
        //Arrange
        GetProduct getProduct = GetProduct.builder()
                                          .name(product.getName())
                                          .description(product.getDescription())
                                          .price(product.getPrice())
                                          .build();
        List<GetReviewDetails> getReviewList = reviews.stream()
                                                      .map(review -> GetReviewDetails.builder()
                                                                                     .sender(review.getSender())
                                                                                     .message(review.getMessage())
                                                                                     .product(getProduct)
                                                                                     .numberOfStars(
                                                                                             review.getNumberOfStars())
                                                                                     .build())
                                                      .toList();
        String user = "user1";

        //Act
        ResultActions response = mvc.perform(get(baseUrl).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isOk())
                .andExpect(
                        content().json(mapper.writeValueAsString(List.of(getReviewList.get(0), getReviewList.get(2)))));
    }

    @Test
    void deleteReview_reviewExists_responseNoContentAndReviewIsDeleted() throws Exception {
        //Arrange
        long reviewId = 1L;
        String user = "user1";

        //Act
        ResultActions response = mvc.perform(
                delete(baseUrl + "/{id}", reviewId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isNoContent());
        Optional<Review> isReview = reviewRepository.findById(reviewId);
        assertThat(isReview).isEmpty();
    }

    @Test
    void deleteReview_reviewDoesNotExist_responseNotFound() throws Exception {
        //Arrange
        long reviewId = 99L;
        String user = "user1";

        //Act
        ResultActions response = mvc.perform(
                delete(baseUrl + "/{id}", reviewId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void deleteReview_reviewIsNotOwned_responseBadRequest() throws Exception {
        //Arrange
        long reviewId = 1L;
        String user = "user2";

        //Act
        ResultActions response = mvc.perform(
                delete(baseUrl + "/{id}", reviewId).header(HttpHeaders.AUTHORIZATION, generateTokenUser(user)));

        //Assert
        response.andExpect(status().isBadRequest());
    }

    @Test
    void updateReview_reviewExists_responseNoContentAndIsSaved() throws Exception {
        //Arrange
        long reviewId = 1L;
        UpdateReview updateReview = new UpdateReview("new message", 1);
        String user = "user1";

        //Act
        ResultActions response = mvc.perform(put(baseUrl + "/{id}", reviewId).header(HttpHeaders.AUTHORIZATION,
                                                                                     generateTokenUser(user))
                                                                             .contentType(MediaType.APPLICATION_JSON)
                                                                             .content(mapper.writeValueAsString(
                                                                                     updateReview)));

        //Assert
        response.andExpect(status().isNoContent());
        Review review = reviewRepository.getReferenceById(reviewId);
        assertThat(review.getId()).isEqualTo(reviewId);
        assertThat(review.getSender()).isEqualTo(user);
        assertThat(review.getMessage()).isEqualTo(updateReview.message());
        assertThat(review.getNumberOfStars()).isEqualTo(updateReview.numberOfStars());
    }

    @Test
    void updateReview_reviewDoesNotExist_responseNotFound() throws Exception {
        //Arrange
        long reviewId = 99L;
        UpdateReview updateReview = new UpdateReview("new message", 1);
        String user = "user1";

        //Act
        ResultActions response = mvc.perform(put(baseUrl + "/{id}", reviewId).header(HttpHeaders.AUTHORIZATION,
                                                                                     generateTokenUser(user))
                                                                             .contentType(MediaType.APPLICATION_JSON)
                                                                             .content(mapper.writeValueAsString(
                                                                                     updateReview)));

        //Assert
        response.andExpect(status().isNotFound());
    }

    @Test
    void updateReview_reviewNotOwned_responseBadRequest() throws Exception {
        //Arrange
        long reviewId = 1L;
        UpdateReview updateReview = new UpdateReview("new message", 1);
        String user = "user2";

        //Act
        ResultActions response = mvc.perform(put(baseUrl + "/{id}", reviewId).header(HttpHeaders.AUTHORIZATION,
                                                                                     generateTokenUser(user))
                                                                             .contentType(MediaType.APPLICATION_JSON)
                                                                             .content(mapper.writeValueAsString(
                                                                                     updateReview)));

        //Assert
        response.andExpect(status().isBadRequest());
    }
}
