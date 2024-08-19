package com.bogdan.shop.services.impl;

import com.bogdan.shop.controllers.models.CreateReviewDto;
import com.bogdan.shop.controllers.models.GetProductDto;
import com.bogdan.shop.controllers.models.GetReviewDetailsDto;
import com.bogdan.shop.controllers.models.UpdateReviewDto;
import com.bogdan.shop.util.exceptions.ResourceDoesNotExistException;
import com.bogdan.shop.persistence.entities.Product;
import com.bogdan.shop.persistence.entities.Review;
import com.bogdan.shop.persistence.repositories.ProductRepository;
import com.bogdan.shop.persistence.repositories.ReviewRepository;
import com.bogdan.shop.services.ReviewService;
import com.bogdan.shop.util.exceptions.ResourceNotOwnedException;
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
    public void createReview(String username, CreateReviewDto review) {
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
    public List<GetReviewDetailsDto> getReviewsSender(String sender) {
        return reviewRepository.findBySender(sender)
                               .stream()
                               .map(this::mapReviewToGetReviewDetailsDto)
                               .toList();
    }

    @Override
    public void deleteReview(Long id, String sender) {
        Review review = reviewRepository.findById(id)
                                        .orElseThrow(() -> new ResourceDoesNotExistException(
                                                "Could not find review with id " + id));
        if (!Objects.equals(review.getSender(), sender)) {
            throw new ResourceNotOwnedException("User does not own this review!");
        }
        reviewRepository.delete(review);
    }

    @Override
    public void updateReview(UpdateReviewDto updateReviewDto, Long id, String sender) {
        Review review = reviewRepository.findById(id)
                                        .orElseThrow(() -> new ResourceDoesNotExistException(
                                                "Could not find review with id " + id));
        if (!Objects.equals(review.getSender(), sender)) {
            throw new ResourceNotOwnedException("User does not own this review!");
        }
        review.setMessage(updateReviewDto.message());
        review.setNumberOfStars(updateReviewDto.numberOfStars());
        reviewRepository.save(review);
    }

    private GetReviewDetailsDto mapReviewToGetReviewDetailsDto(Review review) {
        GetProductDto productDto = productRepository.findByReviewsContains(List.of(review))
                                                    .map(this::mapProductToGetProductDto)
                                                    .orElseThrow(RuntimeException::new);
        return GetReviewDetailsDto.builder()
                                  .product(productDto)
                                  .sender(review.getSender())
                                  .message(review.getMessage())
                                  .numberOfStars(review.getNumberOfStars())
                                  .build();
    }

    private GetProductDto mapProductToGetProductDto(Product product) {
        return GetProductDto.builder()
                            .name(product.getName())
                            .description(product.getDescription())
                            .price(product.getPrice())
                            .build();
    }
}
