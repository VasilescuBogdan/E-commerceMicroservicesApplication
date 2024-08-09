package com.bogdan.shop.controllers.api;

import com.bogdan.shop.controllers.models.AddReviewDto;
import com.bogdan.shop.controllers.models.GetReviewDto;
import com.bogdan.shop.controllers.models.UpdateReviewDto;
import com.bogdan.shop.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    public void addReview(@RequestBody AddReviewDto review, Principal principal) {
        service.addReview(principal.getName(), review);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.OK)
    public List<GetReviewDto> getReviewsSender(Principal principal) {
        return service.getReviewsSender(principal.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable("id") Long id, Principal principal) {
        service.deleteReview(id, principal.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateReview(@PathVariable("id") Long id, @RequestBody UpdateReviewDto reviewDto, Principal principal) {
        service.updateReview(reviewDto, id, principal.getName());
    }
}
