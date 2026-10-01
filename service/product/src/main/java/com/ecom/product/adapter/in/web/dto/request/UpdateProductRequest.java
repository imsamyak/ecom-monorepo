package com.ecom.product.adapter.in.web.dto.request;
import java.util.Optional;
import com.ecom.product.domain.enums.ProductStatus;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateProductRequest {
    private Optional<String> title;
    private Optional<String> description;
    private Optional<Double> price;
    private Optional<ProductStatus> status;
}
