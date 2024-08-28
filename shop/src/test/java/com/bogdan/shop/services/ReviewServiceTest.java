package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.GetProduct;
import com.bogdan.shop.controllers.models.GetReviewDetails;
import com.bogdan.shop.controllers.models.UpdateReview;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.entities.Review;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import com.bogdan.shop.persistence.repositories.ReviewRepository;
import com.bogdan.shop.services.impl.ReviewServiceImpl;
import com.bogdan.shop.util.exceptions.ResourceDoesNotExistException;
import com.bogdan.shop.util.exceptions.ResourceNotOwnedException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ReviewServiceImpl service;

    @Test
    void getReviewSender_repositoryReturnsReviews_returnReviewDetails() {
        //Arrange
        Review review1 = new Review(1L, "user1", "is good", 4);
        Review review2 = new Review(3L, "user1", "is bad", 1);
        Product product = new Product(1L, "name", 30.5F, "description", null, null);
        Mockito.when(reviewRepository.findBySender("user1"))
               .thenReturn(List.of(review1, review2));
        Mockito.when(productRepository.findByReviewsContains(List.of(review1)))
               .thenReturn(Optional.of(product));
        Mockito.when(productRepository.findByReviewsContains(List.of(review2)))
               .thenReturn(Optional.of(product));


        //Act
        List<GetReviewDetails> actualReviews = service.getReviewsSender("user1");

        //Assert
        Assertions.assertThat(actualReviews)
                  .hasSize(2)
                  .isNotNull();
        Assertions.assertThat(actualReviews.get(0))
                  .isEqualTo(mapReviewToGetReviewDetails(review1));
        Assertions.assertThat(actualReviews.get(1))
                  .isEqualTo(mapReviewToGetReviewDetails(review2));
    }

    @Test
    void deleteReview_repositoryReturnsNothing_throwResourceNotFoundException() {
        //Arrange
        Long reviewId = 1L;
        String user = "user";
        Mockito.when(reviewRepository.findById(reviewId))
               .thenReturn(Optional.empty());

        //Assert
        Assertions.assertThatExceptionOfType(ResourceDoesNotExistException.class)
                  .isThrownBy(() -> service.deleteReview(reviewId, user))
                  .withMessage("Could not find review with id " + reviewId);
    }

    @Test
    void deleteReview_repositoryReturnsReviewNotOwnedByUser_throwResourceNotOwnedException() {
        //Arrange
        Long reviewId = 1L;
        String user = "user";

        Mockito.when(reviewRepository.findById(reviewId))
               .thenReturn(Optional.of(new Review(1L, "user2", "", 0)));

        //Assert
        Assertions.assertThatExceptionOfType(ResourceNotOwnedException.class)
                  .isThrownBy(() -> service.deleteReview(reviewId, user))
                  .withMessage("User does not own this review!");
    }

    @Test
    void updateReview_repositoryReturnsNothing_throwResourceNotFoundException() {
        //Arrange
        Long reviewId = 1L;
        String user = "user";
        UpdateReview updateReview = new UpdateReview("new message", 0);
        Mockito.when(reviewRepository.findById(reviewId))
               .thenReturn(Optional.empty());

        //Assert
        Assertions.assertThatExceptionOfType(ResourceDoesNotExistException.class)
                  .isThrownBy(() -> service.updateReview(updateReview, reviewId, user))
                  .withMessage("Could not find review with id " + reviewId);
    }

    @Test
    void updateReview_repositoryReturnsReviewNotOwnedByUser_throwResourceNotOwnedException() {
        //Arrange
        Long reviewId = 1L;
        String user = "user";
        UpdateReview updateReview = new UpdateReview("new message", 0);
        Mockito.when(reviewRepository.findById(reviewId))
               .thenReturn(Optional.of(new Review(1L, "user2", "", 0)));

        //Assert
        Assertions.assertThatExceptionOfType(ResourceNotOwnedException.class)
                  .isThrownBy(() -> service.updateReview(updateReview, reviewId, user))
                  .withMessage("User does not own this review!");
    }

    private GetReviewDetails mapReviewToGetReviewDetails(Review review) {
        GetProduct product = productRepository.findByReviewsContains(List.of(review))
                                              .map(this::mapProductToGetProduct)
                                              .orElseThrow(() -> new ResourceDoesNotExistException(
                                                      "This review is for no product!"));
        return GetReviewDetails.builder()
                               .product(product)
                               .sender(review.getSender())
                               .message(review.getMessage())
                               .numberOfStars(review.getNumberOfStars())
                               .build();
    }

    private GetProduct mapProductToGetProduct(Product product) {
        return GetProduct.builder()
                         .name(product.getName())
                         .description(product.getDescription())
                         .price(product.getPrice())
                         .build();
    }
}
