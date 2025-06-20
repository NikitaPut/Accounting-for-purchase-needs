package com.company.jmix.security;

import com.company.jmix.entity.Need;
import com.company.jmix.entity.NeedPeriod;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "User Role", code = "user-role")
public interface UserRole {
    @EntityPolicy(entityClass = Need.class,
            actions = {EntityPolicyAction.READ, EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE, EntityPolicyAction.DELETE})
    @EntityAttributePolicy(entityClass = Need.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = NeedPeriod.class, actions = EntityPolicyAction.READ)
    @EntityAttributePolicy(entityClass = NeedPeriod.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @ViewPolicy(viewIds = {
            "LoginView",
            "NeedCategory.list",
            "MainView",
    })
    @MenuPolicy(menuIds = {"MainView"})


    void userPermissions();
}