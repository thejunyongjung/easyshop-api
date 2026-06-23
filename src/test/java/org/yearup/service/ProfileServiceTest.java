package org.yearup.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import org.yearup.models.Profile;
import org.yearup.repository.ProfileRepository;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Sql(scripts = "classpath:test-insert-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProfileServiceTest
{
    @Autowired
    private ProfileRepository profileRepository;

    private ProfileService profileService;
    private int userId;

    @BeforeEach
    public void setUp()
    {
        // arrange (shared)
        profileService = new ProfileService(profileRepository);
        userId = profileRepository.findAll().get(0).getUserId();
    }

    @Test
    public void getByUserId_shouldReturn_theProfile()
    {
        // act
        Profile profile = profileService.getByUserId(userId);

        // assert
        assertNotNull(profile, "Because the seeded user has a profile.");
    }

    @Test
    public void getByUserId_shouldReturn_nullWhenNoProfile()
    {
        // act
        Profile profile = profileService.getByUserId(623);

        // assert
        assertNull(profile, "Because no profile exists for user with id 623");
    }

    @Test
    public void update_shouldPersist_theChangedFields()
    {
        // arrange - a profile with changed fields
        Profile changes = new Profile(0, "Remsey", "Mailjard", "415-123-7890",
                "thebestinstructor.skiils4it.nl", "100 Market St", "San Francisco", "CA", "94102");

        // act - update, then read it back
        profileService.update(userId, changes);
        Profile updatedProfile = profileService.getByUserId(userId);

        // assert
        assertEquals("Remsey", updatedProfile.getFirstName(), "Because updated profile should persist the best Java instructor's name");

    }
}
