package com.siemens.internship;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Name must not be blank") //Required field
    private String name;

    @NotBlank(message = "Description must not be blank") //Required field
    private String description;

    @NotBlank(message = "Status must not be blank") //Required field
    private String status;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format") //Ensure email format is valid
    private String email;
}