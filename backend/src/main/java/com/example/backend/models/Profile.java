package com.example.backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;

    private String firstname;

    private String lastname;

    private String bio;

    private String education;

    private String profilePictureUrl;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Date createdAt;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<Skill> skills;

    // Posts created by this profile
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    // Teams this profile is part of
    @ManyToMany(mappedBy = "members")
    private List<Team> teams;

    // Projects created by this profile
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> createdProjects;

    // Projects where this profile is a team member
    @ManyToMany(mappedBy = "members")
    private List<Project> projects;

    @OneToMany(mappedBy = "assignedTo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> assignedTasks;

    @OneToMany(mappedBy = "reviewed", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> receivedReviews;

    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> givenReviews;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }
}
