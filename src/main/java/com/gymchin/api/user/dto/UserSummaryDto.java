package com.gymchin.api.user.dto;

import java.time.OffsetDateTime;
import java.util.Set;

public class UserSummaryDto {
    private final Long userId;
    private final String email;
    private final String nickname;
    private final String gender;
    private final Integer age;
    private final String city;
    private final String district;
    private final String fitnessLevel;
    private final Set<String> goals;
    private final Set<String> preferredDays;
    private final Set<String> preferredTimeSlots;
    private final String coachOption;
    private final String gymName;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public UserSummaryDto(
        Long userId,
        String email,
        String nickname,
        String gender,
        Integer age,
        String city,
        String district,
        String fitnessLevel,
        Set<String> goals,
        Set<String> preferredDays,
        Set<String> preferredTimeSlots,
        String coachOption,
        String gymName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
    ) {
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.city = city;
        this.district = district;
        this.fitnessLevel = fitnessLevel;
        this.goals = goals;
        this.preferredDays = preferredDays;
        this.preferredTimeSlots = preferredTimeSlots;
        this.coachOption = coachOption;
        this.gymName = gymName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getGender() {
        return gender;
    }

    public Integer getAge() {
        return age;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getFitnessLevel() {
        return fitnessLevel;
    }

    public Set<String> getGoals() {
        return goals;
    }

    public Set<String> getPreferredDays() {
        return preferredDays;
    }

    public Set<String> getPreferredTimeSlots() {
        return preferredTimeSlots;
    }

    public String getCoachOption() {
        return coachOption;
    }

    public String getGymName() {
        return gymName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
