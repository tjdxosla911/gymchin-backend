package com.gymchin.api.user.dto;

public class GymmateSearchCriteria {
    private final String city;
    private final String district;
    private final String fitnessLevel;
    private final String goal;
    private final String day;
    private final String timeSlot;
    private final String coachOption;

    public GymmateSearchCriteria(
        String city,
        String district,
        String fitnessLevel,
        String goal,
        String day,
        String timeSlot,
        String coachOption
    ) {
        this.city = city;
        this.district = district;
        this.fitnessLevel = fitnessLevel;
        this.goal = goal;
        this.day = day;
        this.timeSlot = timeSlot;
        this.coachOption = coachOption;
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

    public String getGoal() {
        return goal;
    }

    public String getDay() {
        return day;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public String getCoachOption() {
        return coachOption;
    }
}
