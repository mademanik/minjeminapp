package com.minjemin.product.service;

import com.minjemin.product.dto.ProductStatDTO;
import com.minjemin.product.dto.RentalStatDTO;

public interface StatService {
    ProductStatDTO totalProduct();
    RentalStatDTO totalRental();
}
