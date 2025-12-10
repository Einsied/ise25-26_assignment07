package de.seuhd.campuscoffee.api.controller;

import de.seuhd.campuscoffee.api.dtos.ReviewDto;
import de.seuhd.campuscoffee.api.mapper.DtoMapper;
import de.seuhd.campuscoffee.api.mapper.ReviewDtoMapper;
import de.seuhd.campuscoffee.api.openapi.CrudOperation;
import de.seuhd.campuscoffee.domain.model.objects.Review;
import de.seuhd.campuscoffee.domain.ports.api.CrudService;
import de.seuhd.campuscoffee.domain.ports.api.ReviewService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.seuhd.campuscoffee.api.openapi.Operation.*;
import static de.seuhd.campuscoffee.api.openapi.Resource.REVIEW;

/**
 * Controller for handling reviews for POS, authored by users.
 */
@Tag(name="Reviews", description="Operations for managing reviews for points of sale.")
@Controller
@RequestMapping("/api/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController extends CrudController<Review, ReviewDto, Long> {
    private final ReviewService reviewService;
    private final ReviewDtoMapper reviewDtoMapper;

    @Override
    protected @NonNull CrudService<Review, Long> service() {
        return reviewService;
    }

    @Override
    protected @NonNull DtoMapper<Review, ReviewDto> mapper() {
        return reviewDtoMapper;
    }

    @Operation
    @CrudOperation(operation=GET_ALL, resource=REVIEW)
    @GetMapping("")
    public @NonNull ResponseEntity<List<ReviewDto>> getAll() {
        return super.getAll();
    }

    @Operation
    @CrudOperation(operation=GET_BY_ID, resource=REVIEW)
    @GetMapping("/{id}")
    public @NonNull ResponseEntity <ReviewDto> getById (
            @Parameter(description="Unique identifier of the review to retrieve.", required=true)
            @PathVariable Long id) {
                return super.getById(id);
    }

    @Operation
    @CrudOperation(operation=CREATE, resource=REVIEW)
    @PostMapping("")
    public @NonNull ResponseEntity <ReviewDto> create (
            @Parameter(description="Data of the review to create.", required=true)
            @RequestBody @Valid ReviewDto reviewDto) {
        return super.create(reviewDto);
    }

    @Operation
    @CrudOperation(operation=UPDATE, resource=REVIEW)
    @PutMapping("/{id}")
    public @NonNull ResponseEntity <ReviewDto> update (
            @Parameter(description="Unique identifier of the review to update.", required=true)
            @PathVariable Long id,
            @Parameter(description="Data of the review to update.", required=true)
            @RequestBody @Valid ReviewDto reviewDto) {
        return super.update(id, reviewDto);
    }

    @Operation
    @CrudOperation(operation=DELETE, resource=REVIEW)
    @DeleteMapping("/{id}")
    public @NonNull ResponseEntity <Void> delete (
            @Parameter(description="Unique identifier of the review to delete.", required=true)
            @PathVariable Long id) {
        return super.delete(id);
    }

    @Operation
    @CrudOperation(operation=FILTER, resource=REVIEW)
    @GetMapping("/filter")
    public ResponseEntity <List<ReviewDto>> filter (
        @RequestParam ("pos_id") Long posId ,
        @RequestParam ("approved") Boolean approved
    ) {
        return ResponseEntity.ok(
            reviewService.filter(posId, approved).stream()
            .map(reviewDtoMapper::fromDomain).toList()
        );
    }

    @Operation
    @CrudOperation(operation=UPDATE, resource=REVIEW)
    @PostMapping("/approve")
    public ResponseEntity <ReviewDto> approve (
        @PathVariable Long id ,
        @RequestParam("user_id") Long userId
    ) {
        return ResponseEntity.ok(
            reviewDtoMapper.fromDomain(reviewService.approve(reviewService.getById(id), userId))
        );
    }
}
