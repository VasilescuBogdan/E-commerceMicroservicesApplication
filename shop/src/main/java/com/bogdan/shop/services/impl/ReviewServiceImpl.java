package com.bogdan.shop.services.impl;

import com.bogdan.shop.controllers.models.AddReviewDto;
import com.bogdan.shop.controllers.models.GetProductReviewDto;
import com.bogdan.shop.controllers.models.GetReviewDto;
import com.bogdan.shop.controllers.models.UpdateReviewDto;
import com.bogdan.shop.exceptions.ResourceDoesNotExistException;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.entities.Review;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import com.bogdan.shop.persistence.repositories.ReviewRepository;
import com.bogdan.shop.services.ReviewService;
import com.bogdan.shop.exceptions.ResourceNotOwnedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;

    private final ProductRepository productRepository;

    @Override
    public void addReview(String username, AddReviewDto review) {
        Review save = reviewRepository.save(Review.builder()
                                                  .numberOfStars(review.numberOfStars())
                                                  .sender(username)
                                                  .message(review.message())
                                                  .build());
        Product product = productRepository.getReferenceById(review.productId());
        product.getReviews()
               .add(save);
        productRepository.save(product);
    }

    @Override
    public List<GetReviewDto> getReviewsSender(String sender) {
        return reviewRepository.findBySender(sender)
                               .stream()
                               .map(this::mapReviewToGetReviewDto)
                               .toList();
    }

    @Override
    public void deleteReview(Long id, String sender) {
        validateReview(id, sender);
        reviewRepository.deleteById(id);
    }

    @Override
    public void updateReview(UpdateReviewDto updateReviewDto, Long id, String sender) {
        Review review = validateReview(id, sender);
        review.setMessage(updateReviewDto.message());
        review.setNumberOfStars(updateReviewDto.numberOfStars());
        reviewRepository.save(review);
    }

    private Review validateReview(Long id, String sender) {
        Review review = reviewRepository.findById(id)
                                        .orElseThrow(() -> new ResourceDoesNotExistException("Could not find review with id " + id));
        if (!Objects.equals(review.getSender(), sender)) {
            throw new ResourceNotOwnedException("User does not own this review!");
        }
        return review;
    }

    private GetReviewDto mapReviewToGetReviewDto(Review review) {
        GetProductReviewDto productDto = productRepository.findByReviewsContains(List.of(review))
                                                          .map(this::mapProductToGetProductReviewDto)
                                                          .orElseThrow(RuntimeException::new);
        return GetReviewDto.builder()
                           .product(productDto)
                           .sender(review.getSender())
                           .message(review.getMessage())
                           .numberOfStars(review.getNumberOfStars())
                           .build();
    }

    private GetProductReviewDto mapProductToGetProductReviewDto(Product product) {
        return GetProductReviewDto.builder()
                                  .name(product.getName())
                                  .description(product.getDescription())
                                  .price(product.getPrice())
                                  .build();
    }
}
