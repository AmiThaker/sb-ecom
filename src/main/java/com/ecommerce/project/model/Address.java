package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@ToString
@Table(name="addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min=5,message="Street must be atleast 5 characters")
    private String street;

    @NotBlank
    @Size(min=5,message="Building Name must be atleast 5 characters")
    private String buildingName;

    @NotBlank
    @Size(min=5,message="City name must be atleast 5 characters")
    private String city;

    @NotBlank
    @Size(min=5,message="State name must be atleast 5 characters")
    private String state;

    @NotBlank
    @Size(min=5,message="Country name must be atleast 5 characters")
    private String country;

    @NotBlank
    @Size(min=5,message="Pincode must be atleast 6 characters")
    private String pincode;


    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

}
