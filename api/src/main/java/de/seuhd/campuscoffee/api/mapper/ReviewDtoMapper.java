package de.seuhd.campuscoffee.api.mapper;

import de.seuhd.campuscoffee.api.dtos.ReviewDto;
import de.seuhd.campuscoffee.domain.model.objects.Review;
import org.mapstruct.Mapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

/**
 * MapStruct mapper for converting between the {@link Review} domain model objects and {@link ReviewDto}s.
 * .
 */
@Mapper(componentModel = "spring")
@ConditionalOnMissingBean // prevent IntelliJ warning about duplicate beans
public abstract class ReviewDtoMapper implements DtoMapper<Review, ReviewDto> {
    @Autowired
    @SuppressWarnings("unused") // used in @Mapping expressions
    protected PosService posService;
    @Autowired
    @SuppressWarnings("unused") // used in @Mapping expressions
    protected UserService userService;

    @Mapping(target = "posId", expression = "java(source.pos().getId())")
    @Mapping(target = "authorId", expression = "java(source.author().getId())")
    public abstract ReviewDto fromDomain(Review source);

    @Mapping(target = "pos", expression = "java(posService.getById(source.posId()))")
    @Mapping(target = "author", expression = "java(userService.getById(source.authorId()))")
    @Mapping(target = "approved", constant = "false")
    @Mapping(target = "approvalCount", constant = "0")
    public abstract Review toDomain(ReviewDto source);
}
