package com.minjemin.product.dto;

import com.minjemin.product.model.RentalStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class RentalStatDTO {
    private Integer totalRental;
    private List<RentalDTO> dataRentals;
    private Map<RentalStatus, Long> statuses;
}
