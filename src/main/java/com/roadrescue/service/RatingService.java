package com.roadrescue.service;

import com.roadrescue.model.Rating;
import com.roadrescue.model.User;
import com.roadrescue.repository.RatingRepository;
import com.roadrescue.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    @Autowired
    public RatingService(RatingRepository ratingRepository, UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
    }

    public Rating submitRating(Long requestId, Long helperId, Long customerId, int stars, String review) {
        Rating rating = new Rating();
        rating.setRequestId(requestId);
        rating.setHelperId(helperId);
        rating.setCustomerId(customerId);
        rating.setStars(stars);
        rating.setReview(review);
        Rating saved = ratingRepository.save(rating);

        User helper = userRepository.findById(helperId).orElseThrow();
        double currentTotal = helper.getAverageRating() * helper.getRatingCount();
        int newCount = helper.getRatingCount() + 1;
        double newAverage = (currentTotal + stars) / newCount;
        helper.setAverageRating(Math.round(newAverage * 10.0) / 10.0);
        helper.setRatingCount(newCount);
        userRepository.save(helper);

        return saved;
    }
}
