package com.progIII.ms_security.Controllers;

import com.progIII.ms_security.Models.Role;
import com.progIII.ms_security.Models.User;
import com.progIII.ms_security.Models.UserRole;
import com.progIII.ms_security.Repositories.RoleRepository;
import com.progIII.ms_security.Repositories.UserRepository;
import com.progIII.ms_security.Repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/user_role")
public class UsersRolesController {
    @Autowired
    private UserRoleRepository theUserRoleRepository;
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private RoleRepository theRoleRepository;

    @PostMapping("user/{userId}/role/{roleId}")
    public UserRole create(@PathVariable String userId, @PathVariable String roleId){
        User theUser=this.theUserRepository.findById(userId).orElse(null);
        Role theRole=this.theRoleRepository.findById(roleId).orElse(null);
        if(theUser!=null && theRole!=null){
            UserRole newUserRole=new UserRole();
            newUserRole.setUser(theUser);
            newUserRole.setRole(theRole);
            return this.theUserRoleRepository.save(newUserRole);
        }else{
            return null;
        }
    }
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id){
        UserRole theUserRole=this.theUserRoleRepository.findById(id).orElse(null);
        if (theUserRole!=null){
            this.theUserRoleRepository.delete(theUserRole);
        }
    }
    @GetMapping("user/{userId}")
    public List<UserRole> getRolesbyUser(@PathVariable String userId){
        return this.theUserRoleRepository.getRolesByUser(userId);
    }
    @GetMapping("role/{roleId}")
    public List<UserRole> getUsersbyRole(@PathVariable String roleId) {
        return this.theUserRoleRepository.getUsersByRole(roleId);
    }
}
