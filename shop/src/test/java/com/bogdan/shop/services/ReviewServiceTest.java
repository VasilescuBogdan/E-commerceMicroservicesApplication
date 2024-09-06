package com.bogdan.shop.services;

import com.bogdan.shop.controllers.models.CreateReview;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ReviewServiceImpl service;

    @Test
    void createReview_productRepositoryCallsSaveMethod() {
        //Arrange
        String username = "user";
        CreateReview createReview = new CreateReview(1L, 3, "it's good");
        Product product = new Product(createReview.productId(), "product", 15F, "this is product", new ArrayList<>(),
                new ArrayList<>());
        Review review = new Review(1L, username, createReview.message(), createReview.numberOfStars(), product);
        doReturn(review).when(reviewRepository)
                        .save(new Review(null, review.getSender(), review.getMessage(), review.getNumberOfStars(),
                                product));
        doReturn(product).when(productRepository)
                         .getReferenceById(createReview.productId());
        product.getReviews()
               .add(review);

        //Act
        service.createReview(username, createReview);

        //Assert
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void getReviewSender_repositoryReturnsReviews_returnReviewDetails() {
        //Arrange
        Product product = new Product(1L, "name", 30.5F, "description", new ArrayList<>(), new ArrayList<>());
        Review review1 = new Review(1L, "user1", "is good", 4, product);
        Review review2 = new Review(3L, "user1", "is bad", 1, product);
        doReturn(product).when(productRepository)
                         .findByReviewsContains(List.of(review2));
        doReturn(product).when(productRepository)
                         .findByReviewsContains(List.of(review1));
        doReturn(List.of(review1, review2)).when(reviewRepository)
                                           .findBySender("user1");

        //Act
        List<GetReviewDetails> actualReviews = service.getReviewsSender("user1");

        //Assert
        assertThat(actualReviews).hasSize(2)
                                 .isNotNull();
        assertThat(actualReviews.get(0)).isEqualTo(mapReviewToGetReviewDetails(review1));
        assertThat(actualReviews.get(1)).isEqualTo(mapReviewToGetReviewDetails(review2));
    }

    @Test
    void deleteReview_repositoryReturnsReview_repositoryCallsDelete() {
        //Arrange
        Long reviewId = 1L;
        String user = "user";
        Review review = new Review(reviewId, user, "is good", 5, null);
        doReturn(Optional.of(review)).when(reviewRepository)
                                     .findById(reviewId);

        //Act
        service.deleteReview(reviewId, user);

        //Assert
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void deleteReview_repositoryReturnsNothing_throwResourceNotFoundException() {
        //Arrange
        Long reviewId = 1L;
        String user = "user";
        doReturn(Optional.empty()).when(reviewRepository)
                                  .findById(reviewId);

        //Assert
        assertThatExceptionOfType(ResourceDoesNotExistException.class).isThrownBy(() -> {
                                                                          //Act
                                                                          service.deleteReview(reviewId, user);
                                                                      })
                                                                      .withMessage("Could not find review with id " +
                                                                                   reviewId);
    }

    @Test
    void deleteReview_repositoryReturnsReviewNotOwnedByUser_throwResourceNotOwnedException() {
        //Arrange
        Long reviewId = 1L;
        String user = "user";
        doReturn(Optional.of(new Review(reviewId, "user2", "", 0, null))).when(reviewRepository)
                                                                         .findById(reviewId);

        //Assert
        assertThatExceptionOfType(ResourceNotOwnedException.class).isThrownBy(() -> {
                                                                      //Act
                                                                      service.deleteReview(reviewId, user);
                                                                  })
                                                                  .withMessage("User does not own this review!");
    }

    @Test
    void updateReview_repositoryReturnsReview_repositoryCallsSave() {
        //Arrange
        Long reviewId = 1L;
        String user = "user";
        UpdateReview updateReview = new UpdateReview("new message", 0);
        Review review = new Review(1L, user, "", 0, null);
        doReturn(Optional.of(review)).when(reviewRepository)
                                     .findById(reviewId);

        //Act
        service.updateReview(updateReview, reviewId, user);

        //Assert
        verify(reviewRepository, times(1)).save(
                new Review(review.getId(), review.getSender(), updateReview.message(), updateReview.numberOfStars(),
                        review.getProduct()));
    }

    @Test
    void updateReview_repositoryReturnsNothing_throwResourceNotFoundException() {
        //Arrange
        Long reviewId = 1L;
        String user = "user";
        UpdateReview updateReview = new UpdateReview("new message", 0);
        doReturn(Optional.empty()).when(reviewRepository)
                                  .findById(reviewId);

        //Assert
        assertThatExceptionOfType(ResourceDoesNotExistException.class).isThrownBy(() -> {
                                                                          //Act
                                                                          service.updateReview(updateReview, reviewId
                                                                                  , user);
                                                                      })
                                                                      .withMessage("Could not find review with id " +
                                                                                   reviewId);
    }

    @Test
    void updateReview_repositoryReturnsReviewNotOwnedByUser_throwResourceNotOwnedException() {
        //Arrange
        Long reviewId = 1L;
        String user = "user";
        UpdateReview updateReview = new UpdateReview("new message", 0);
        Review review = new Review(1L, "user2", "", 0, null);
        doReturn(Optional.of(review)).when(reviewRepository)
                                     .findById(reviewId);

        //Assert
        assertThatExceptionOfType(ResourceNotOwnedException.class).isThrownBy(() -> {
                                                                      //Act
                                                                      service.updateReview(updateReview, reviewId,
                                                                              user);
                                                                  })
                                                                  .withMessage("User does not own this review!");
    }

    private GetReviewDetails mapReviewToGetReviewDetails(Review review) {
        GetProduct product = mapProductToGetProduct(productRepository.findByReviewsContains(List.of(review)));
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
