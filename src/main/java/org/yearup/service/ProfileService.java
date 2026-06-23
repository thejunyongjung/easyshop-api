package org.yearup.service;

import org.springframework.stereotype.Service;
import org.yearup.models.Profile;
import org.yearup.repository.ProfileRepository;

@Service
public class ProfileService
{
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository)
    {
        this.profileRepository = profileRepository;
    }

    public Profile create(Profile profile)
    {
        return profileRepository.save(profile);
    }

    public Profile getByUserId(int userId)
    {
        return profileRepository.findById(userId).orElse(null);
    }

    public Profile update(int userId, Profile profile)
    {
        Profile oldProfile = profileRepository.findById(userId).orElseThrow();
        oldProfile.setFirstName(profile.getFirstName());
        oldProfile.setLastName(profile.getLastName());
        oldProfile.setPhone(profile.getPhone());
        oldProfile.setEmail(profile.getEmail());
        oldProfile.setAddress(profile.getAddress());
        oldProfile.setCity(profile.getCity());
        oldProfile.setState(profile.getState());
        oldProfile.setZip(profile.getZip());
        return profileRepository.save(oldProfile);
    }
}
