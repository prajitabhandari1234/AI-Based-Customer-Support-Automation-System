package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.UserRole;
import com.cqu.coit13230.AIBasedCustomerSupport.model.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries optional user fields for a partial admin update.
 *
 * <p>
 * This Data Transfer Object (DTO) is used when an administrator
 * performs a partial update of an existing user's information.
 * </p>
 *
 * <p>
 * The request can contain the user's name, role, and account status.
 * Since these fields are optional, the administrator can provide only
 * the values that need to be updated. The DTO transfers the submitted
 * data to the service layer for processing.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    /**
     * Updated name of the user.
     *
     * <p>
     * This field is optional and can be provided when the administrator
     * needs to update the user's name.
     * </p>
     */
    private String name;

    /**
     * Updated role assigned to the user.
     *
     * <p>
     * This field is optional and represents the user's access role
     * within the customer support system.
     * </p>
     */
    private UserRole role;

    /**
     * Updated account status of the user.
     *
     * <p>
     * This field is optional and represents the current status of
     * the user's account within the system.
     * </p>
     */
    private UserStatus status;

}