package de.seuhd.campuscoffee.domain.model.objects;

import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import de.seuhd.campuscoffee.domain.model.objects.Pos;
import de.seuhd.campuscoffee.domain.model.objects.User;

import java.time.LocalDateTime;

/**
 * Domain record that stores a review for a point of sale.
 * Reviews are approved once they received a configurable number of approvals.
 */
@Builder(toBuilder = true)
public record Review(
        @Nullable Long id, // null when the review has not been created yet
        @Nullable LocalDateTime createdAt,
        @Nullable LocalDateTime updatedAt,
        @NonNull Pos pos,
        @NonNull User author,
        @NonNull String review,
        @NonNull Integer approvalCount, // is updated by the domain module
        @NonNull Boolean approved // is determined by the domain module
) implements DomainModel<Long> {
    @Override
    public Long getId() {
        return id;
    }
}