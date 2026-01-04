package com.gymchin.api.user.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column
    private String password;

    private String gender;
    private Integer age;
    private String city;
    private String district;
    private String fitnessLevel;
    private String coachOption;
    private String gymName;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_goals", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "goal")
    private Set<String> goals = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_preferred_days", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "preferred_day")
    private Set<String> preferredDays = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_preferred_time_slots", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "time_slot")
    private Set<String> preferredTimeSlots = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false, columnDefinition = "timestamp with time zone")
    private OffsetDateTime updatedAt;

    protected User() {
    }

    public User(String email, String nickname) {
        this.email = email;
        this.nickname = nickname;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPassword() {
        return password;
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

    public String getCoachOption() {
        return coachOption;
    }

    public String getGymName() {
        return gymName;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setFitnessLevel(String fitnessLevel) {
        this.fitnessLevel = fitnessLevel;
    }

    public void setCoachOption(String coachOption) {
        this.coachOption = coachOption;
    }

    public void setGymName(String gymName) {
        this.gymName = gymName;
    }

    public void setGoals(Set<String> goals) {
        this.goals = goals == null ? new HashSet<>() : new HashSet<>(goals);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPreferredDays(Set<String> preferredDays) {
        this.preferredDays = preferredDays == null ? new HashSet<>() : new HashSet<>(preferredDays);
    }

    public void setPreferredTimeSlots(Set<String> preferredTimeSlots) {
        this.preferredTimeSlots = preferredTimeSlots == null ? new HashSet<>() : new HashSet<>(preferredTimeSlots);
    }
}
