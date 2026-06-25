package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Profile;
import org.yearup.service.ProfileService;
import org.yearup.service.UserService;

import java.security.Principal;


@RestController
@RequestMapping("profile")
@CrossOrigin
public class ProfileController {
    private final ProfileService profileService;
    private final UserService userService;

    public ProfileController(ProfileService profileService, UserService userService)
    {
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public Profile getProfile(Principal principal)
    {
        Profile profile = profileService.getByUserId(currentUserId(principal));
        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found");
        }
        return profile;
    }

    @PutMapping("")
    @PreAuthorize("isAuthenticated()")
    public Profile updateProfile(@RequestBody Profile profile, Principal principal)
    {
        return profileService.update(currentUserId(principal), profile);
    }

    /** HELPER METHOD */
    private int currentUserId(Principal principal)
    {
        return userService.getIdByUsername(principal.getName());
    }
}
