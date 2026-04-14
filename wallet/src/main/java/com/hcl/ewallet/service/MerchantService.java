package com.hcl.ewallet.service;

import com.hcl.ewallet.dto.CreateMerchantRequest;
import com.hcl.ewallet.dto.MerchantResponse;

public interface MerchantService {

    MerchantResponse createMerchant(CreateMerchantRequest request);

    MerchantResponse getMerchant(Long merchantId);

    MerchantResponse getMerchantByMerchantId(String merchantId);
}
