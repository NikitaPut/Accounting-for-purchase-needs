package com.company.jmix.service;

import com.company.jmix.entity.User;
import com.company.jmix.security.ManagerRole;
import com.company.jmix.security.UserRole;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserInitializer {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

    @Transactional
    public void createDefaultUsers() {
        systemAuthenticator.withSystem(() -> {
            // Create manager user
            if (dataManager.load(User.class)
                    .query("select u from User u where u.username = :username")
                    .parameter("username", "manager")
                    .optional().isEmpty()) {

                User manager = dataManager.create(User.class);
                manager.setUsername("manager");
                manager.setPassword(passwordEncoder.encode("manager"));
                manager.setFirstName("Manager");
                manager.setLastName("Manager");
                manager.setActive(true);
                dataManager.save(manager);

                // Assign manager role
                RoleAssignmentEntity managerRole = dataManager.create(RoleAssignmentEntity.class);
                managerRole.setUsername("manager");
                managerRole.setRoleCode(ManagerRole.CODE);
                dataManager.save(managerRole);
            }

            // Create regular user
            if (dataManager.load(User.class)
                    .query("select u from User u where u.username = :username")
                    .parameter("username", "user")
                    .optional().isEmpty()) {

                User user = dataManager.create(User.class);
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user"));
                user.setFirstName("Regular");
                user.setLastName("User");
                user.setActive(true);
                dataManager.save(user);

                // Assign user role
                RoleAssignmentEntity userRole = dataManager.create(RoleAssignmentEntity.class);
                userRole.setUsername("user");
                userRole.setRoleCode(UserRole.CODE);
                dataManager.save(userRole);
            }
            return null;
        });
    }
}