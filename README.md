# java-explore-with-me

A multi-module Spring Boot app for managing events, participation, and statistics tracking.

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.4-green.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6-red.svg)](https://maven.apache.org)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17.4-blue.svg)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-27.4-2496ED.svg?logo=docker)](https://www.docker.com)


## Features

### Core Functionality
- **Event Management**
    - CRUD operations
    - Filter events by various criteria
    - Manage event participation requests
- **User System**
    - User registration and management
    - Role-based access control
    - User participation tracking
- **Statistics**
    - Track event views and participation
    - Unique visitor counting
    - Comprehensive statistics API
- **Comments System**
    - Threaded comments with replies
    - Comment moderation
    - Filtering and pagination

### Storage Options
- PostgreSQL relational database
- JPA/Hibernate for data access

### REST API
- Comprehensive REST endpoints
- JSON request/response format
- Proper HTTP status codes
- Input validation and error handling

### Additional Features
- Docker support for easy deployment
- Multi-module Maven project structure
- Pagination and filtering support


## Getting Started

### Prerequisites
- Java 21 or later
- Maven 3.6 or later
- PostgreSQL 17.4 or later (optional, for production)
- Docker 27.4.0 or later (optional, for containerized deployment)

### Clone the Repository
```sh
git clone git@github.com:DawydowGerman/java-explore-with-me.git
  ```
  ```sh
cd java-explore-with-me
  ```

### Build with Maven
  ```sh
  mvn clean package
  ```

### Run the Application

- Option 1: With PostgreSQL (default)
  ```sh
  mvn spring-boot:run
  ```

- Option 2: With Docker
  ```sh
  docker-compose up --build
  ```