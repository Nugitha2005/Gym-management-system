package com.example.GymManagement.access;

import jakarta.persistence.*;
import java.time.LocalDate;

//Student ID : IT25100934
//Name : Artigala A.V.N.M

@Entity
@Table(name = "medical_clearance")

public class MedicalClearance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
}
