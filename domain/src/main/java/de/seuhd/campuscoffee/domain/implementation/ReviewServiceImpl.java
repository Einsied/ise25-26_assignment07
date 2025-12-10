package de.seuhd.campuscoffee.domain.implementation;

import de.seuhd.campuscoffee.domain.exceptions.ValidationException;
import de.seuhd.campuscoffee.domain.exceptions.NotFoundException;
import de.seuhd.campuscoffee.domain.configuration.ApprovalConfiguration;
import de.seuhd.campuscoffee.domain.model.objects.Review;
import de.seuhd.campuscoffee.domain.model.objects.Pos;
import de.seuhd.campuscoffee.domain.ports.api.ReviewService;
import de.seuhd.campuscoffee.domain.ports.data.CrudDataService;
import de.seuhd.campuscoffee.domain.ports.data.PosDataService;
import de.seuhd.campuscoffee.domain.ports.data.ReviewDataService;
import de.seuhd.campuscoffee.domain.ports.data.UserDataService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of the Review service that handles business logic related to review entities.
 */
@Slf4j
@Service
public class ReviewServiceImpl extends CrudServiceImpl<Review, Long> implements ReviewService {
    private final ReviewDataService reviewDataService;
    private final UserDataService userDataService;
    private final PosDataService posDataService;
    private final ApprovalConfiguration approvalConfiguration;

    public ReviewServiceImpl(@NonNull ReviewDataService reviewDataService,
                             @NonNull UserDataService userDataService,
                             @NonNull PosDataService posDataService,
                             @NonNull ApprovalConfiguration approvalConfiguration) {
        super(Review.class);
        this.reviewDataService = reviewDataService;
        this.userDataService = userDataService;
        this.posDataService = posDataService;
        this.approvalConfiguration = approvalConfiguration;
    }

    @Override
    protected CrudDataService<Review, Long> dataService() {
        return reviewDataService;
    }

    @Override
    @Transactional
    public @NonNull Review upsert(@NonNull Review review) {
        // Check if the id exists, good that the test case hinted to it
        try {
            posDataService.getById(review.pos().getId());
        } catch (NullPointerException e) {
            throw new NotFoundException(Pos.class,review.pos().id());
        };

        // If the filter gives us back an entry we create no second one
        List<Review> allReviews = reviewDataService.filter(review.pos(), review.author());
        if (allReviews.size() > 0){
            throw new ValidationException("Every POS can be reviewed only once by a user.");
        }
    
        return super.upsert(review);
    }

    @Override
    @Transactional(readOnly = true)
    public @NonNull List<Review> filter(@NonNull Long posId, @NonNull Boolean approved) {
        return reviewDataService.filter(posDataService.getById(posId), approved);
    }

    @Override
    @Transactional
    public @NonNull Review approve(@NonNull Review review, @NonNull Long userId) {
        log.info("Processing approval request for review with ID '{}' by user with ID '{}'...",
                review.getId(), userId);

        // validate that the user exists
        // Using exceptions for control flow is bad practice but apparently the intended solution
        try {
            userDataService.getById(userId);
        } catch (NotFoundException exception) {
            throw new ValidationException("User Id can not found => User does not exist");
        }

        // validate that the review exists
        // Using exceptions for control flow is bad practice but apparently the intended solution
        try {
            reviewDataService.getById(review.id());
        } catch (NotFoundException exception) {
            throw new ValidationException("Review id can not found => Review does not exist");
        }

        // a user cannot approve their own review
        if (review.author().getId() == userId){
            throw new ValidationException("User can not approve own review");
        };

        // increment approval count
        Integer approvalCount = review.approvalCount() + 1;

        // update approval status to determine if the review now reaches the approval quorum
        Boolean isApproved = false;
        if (approvalCount >= approvalConfiguration.minCount()){
            isApproved = true;
        };
        return reviewDataService.upsert(Review.builder()
            .id(review.id())
            .createdAt(review.createdAt())
            .updatedAt(review.updatedAt())
            .pos(review.pos())
            .author(review.author())
            .review(review.review())
            .approvalCount(approvalCount)
            .approved(isApproved)
            .build()
        );
    }

    /**
     * Calculates and updates the approval status of a review based on the approval count.
     * Business rule: A review is approved when it reaches the configured minimum approval count threshold.
     *
     * @param review The review to calculate approval status for
     * @return The review with updated approval status
     */
    Review updateApprovalStatus(Review review) {
        log.debug("Updating approval status of review with ID '{}'...", review.getId());
        return review.toBuilder()
                .approved(isApproved(review))
                .build();
    }
    
    /**
     * Determines if a review meets the minimum approval threshold.
     * 
     * @param review The review to check
     * @return true if the review meets or exceeds the minimum approval count, false otherwise
     */
    private boolean isApproved(Review review) {
        return review.approvalCount() >= approvalConfiguration.minCount();
    }
}
