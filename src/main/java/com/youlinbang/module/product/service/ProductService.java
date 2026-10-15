package com.youlinbang.module.product.service;

import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.product.dto.ProductCreateDTO;
import com.youlinbang.module.product.vo.ProductVO;

public interface ProductService {

    ProductVO create(Long userId, ProductCreateDTO dto);

    PageResult<ProductVO> page(long page, long size, String keyword, String category);

    ProductVO detail(Long id);
}
