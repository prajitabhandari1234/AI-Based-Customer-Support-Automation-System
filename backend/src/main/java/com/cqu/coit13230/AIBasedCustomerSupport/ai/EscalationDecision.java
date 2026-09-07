package com.cqu.coit13230.AIBasedCustomerSupport.ai;

import java.util.List;

public record EscalationDecision(
        boolean escalate,
        List<String> reasons
) {}
