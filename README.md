# Masandaza Cleaning Service System

## Purpose of this README
This document serves as the formal technical and operational documentation for the Masandaza software application. Its primary purpose is to provide evaluators, stakeholders, and developers with a comprehensive breakdown of the application architecture, functional capabilities, codebase implementation, local setup instructions, and automated quality assurance standards.

---

## Executive Summary & System Purpose
Masandaza is an end-to-end mobile software solution engineered to digitize and streamline the cleaning service industry. The platform addresses common inefficiencies in service coordination by establishing a seamless, real-time digital bridge between everyday clients, operational field staff, and system administrators. By replacing manual booking logs and fragmented communication with centralized cloud database management, Masandaza ensures transparent service tracking, prompt job allocation, and structured operational workflows.

---

## Detailed Functional Modules & Code Breakdown

The application is architected using Kotlin within Android Studio, adhering to object-oriented principles and leveraging Firebase Firestore for real-time NoSQL data persistence and synchronization. Below is the detailed breakdown of the core modules and how the codebase operates:

### 1. Authentication and Role-Based Access Control (RBAC)
- **Components:** `LoginActivity`, `RegisterActivity`, `MainActivity`, User Session Management.
- **Functionality:** 
  - Manages secure user registration and login credentials, validating inputs for proper syntax, email formats, and password strength.
  - Integrates Firebase Authentication to secure user accounts and maintain active login sessions across application restarts.
  - Implements role-based routing upon successful authentication, directing users to distinct dashboard interfaces based on whether they are authenticated as a general client or an operational cleaning staff member.

### 2. Client Booking and Service Discovery Module
- **Components:** `BookingActivity`, `ServicesActivity`, `TrackBookingActivity`, Service Catalogs, and Booking Forms.
- **Functionality:**
  - Presents clients with an organized catalog of cleaning service packages (e.g., standard residential cleaning, deep cleaning, carpet cleaning, and move-in/move-out services).
  - Provides interactive booking forms where users select preferred dates, times, specific service tiers, and service locations.
  - Writes booking records directly to Firebase Firestore, attaching unique identifiers and client metadata.
  - Features real-time tracking interfaces (`TrackBookingActivity`) that allow clients to view the live status of their submitted cleaning requests as they transition through operational stages.

### 3. Field Staff Job Management and Execution Module
- **Components:** `StaffDashboardActivity`, `StaffJobsActivity`, `StaffJobDetailsActivity`, `UpdateJobStatusActivity`, and custom RecyclerView adapters (`StaffJobsAdapter`).
- **Functionality:**
  - **Real-Time Database Synchronization:** Utilizes Firebase Firestore real-time snapshot listeners (`addSnapshotListener`) to instantly fetch newly assigned cleaning jobs without requiring manual page refreshes.
  - **Dynamic List Population:** Binds live query results to high-performance `RecyclerView` layouts (`rvStaffMyJobsList`), dynamically rendering job cards with client details, scheduled times, and specific cleaning requirements.
  - **Job Lifecycle & Status Transitions:** Enables staff members to inspect individual job specifications (`StaffJobDetailsActivity`) and execute status updates (`UpdateJobStatusActivity`). Staff can transition bookings through explicit states—such as `Pending`, `Assigned`, `Cleaning In Progress`, and `Completed`—ensuring total visibility for both administration and clients.

### 4. Layout Architecture and UI/UX Implementation
- **Components:** XML layout files (e.g., `item_staff_job_figma.xml`, activity layouts), ConstraintLayout hierarchies, Material Design components.
- **Functionality:**
  - Implements responsive layouts using advanced `ConstraintLayout` structures to adapt seamlessly across various screen sizes and device densities.
  - Integrates design-time rendering attributes (`tools:text`, `tools:visibility`) to cleanly separate placeholder mockups from runtime data binding, preventing hardcoded data leakage and maintaining robust rendering performance.

---

## Comprehensive Tech Stack
- **Programming Language:** Kotlin (100% native Android development)
- **Integrated Development Environment (IDE):** Android Studio
- **Backend & Database:** Firebase Firestore (Real-time NoSQL cloud database) and Firebase Authentication
- **User Interface & Architecture:** Material Design Components, RecyclerView, ViewBinding, Activities, and Adapters
- **Version Control & Collaboration:** Git & GitHub repository management
- **Continuous Integration (CI/CD):** GitHub Actions (`android_ci.yml`) for automated builds and unit testing

---

## Installation and Local Setup Guide
To clone, build, and run the Masandaza project locally on your machine for evaluation or further development, follow these step-by-step instructions:

1. **Clone the Repository:**
   Open your terminal or command prompt and clone the project repository using Git:
   ```bash
   git clone [https://github.com/st10448097/Masandaza.git](https://github.com/st10448097/Masandaza.git)
