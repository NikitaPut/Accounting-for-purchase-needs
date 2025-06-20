package com.company.jmix.security;

import com.company.jmix.entity.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.*;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

import java.util.Arrays;
import java.util.Collection;

@ResourceRole(name = "Manager Role", code = "manager-role")
public interface ManagerRole {
    @EntityPolicy(entityClass = NeedType.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = NeedType.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = NeedCategory.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = NeedCategory.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = NeedPeriod.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = NeedPeriod.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Need.class, actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityClass = Need.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @ViewPolicy(viewIds = {"*"})
    @MenuPolicy(menuIds = {"*"})
    void managerPermissions();
}