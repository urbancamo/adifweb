# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ADIF Web is a Spring Boot web application that provides a web interface for processing ADIF (Amateur Data Interchange Format) files used in amateur radio. The application transforms ADIF and CSV files into various output formats including KML (Google Earth), augmented ADIF, markdown contact lists, and QSL labels.

## Core Architecture

### Spring Boot Structure
- **Main Application**: `ProcessingControlApplication.java` - Entry point with JPA repository scanning
- **Configuration**: `ApplicationConfiguration.java` - Main configuration class that initializes services and external dependencies
- **Component Scanning**: Scans both `uk.m0nom.adifproc` (core processor) and `uk.m0nom.adifweb` (web interface) packages

### Key Components

**Controllers** (`src/main/java/uk/m0nom/adifweb/controller/`):
- `UploadController` - Main form processing and file upload handling
- `ResultsController` - Displays processing results
- `DownloadController` - Handles file downloads
- `IndexController` - Home page routing
- `CoordinateConverterController` - Coordinate conversion utilities

**API Controllers** (`src/main/java/uk/m0nom/adifweb/api/`):
- `ActivityApiController` - REST API for activity reference lookups
- `LocationApiController` - REST API for location services

**Services**:
- `ActivityService` - Handles amateur radio activity reference validation (SOTA, POTA, etc.)
- `TransformerService` - Core ADIF file transformation logic
- `FileService` - File handling operations
- `ValidatorService` - Comprehensive validation system for amateur radio data

**Domain Models** (`src/main/java/uk/m0nom/adifweb/domain/`):
- `HtmlParameters` - Web form parameter binding
- `ActivitySearchResult`/`LocationSearchResult` - API response models
- `ControlInfo` - Processing control information

### Validation System
The application includes extensive validation for amateur radio specific data:
- Activity references (SOTA, POTA, WWFF, IOTA, etc.)
- Callsigns, coordinates, grid squares
- Satellite information and antenna specifications
- Located in `src/main/java/uk/m0nom/adifweb/validation/`

### Data Sources
- DXCC entities and country data loaded at startup
- Activity databases for various amateur radio programs
- Satellite orbital data from NORAD files in `src/main/resources/norad/`

## Development Commands

### Build and Run
```bash
# Build the application
./mvnw clean package

# Run with development profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Run specific test
./mvnw test -Dtest=LocationApiControllerTest
```

### Maven Goals
```bash
# Create deployment package with platform configs
./mvnw clean package spring-boot:repackage antrun:run@repackage
```

## Configuration

### Application Profiles
- **dev**: Development with local websocket (localhost:9090)
- **prod**: Production configuration
- **test**: Test environment

### Environment Variables
- `QRZ_USERNAME`/`QRZ_PASSWORD` - QRZ.com API credentials
- `AWS_ACCESS_KEY`/`AWS_SECRET_KEY` - AWS services (optional)

### Key Properties
- Default server port: Configurable via `server.port`
- File upload limits: 128KB max file/request size
- Session configuration: SameSite=none for cookies

## Testing

### HTTP Tests
REST API tests are located in `src/test/http/`:
- `activity-api/get-tests.http` - Activity API endpoint tests
- `location-api/get-tests.http` - Location API tests
- Default test server: `http://localhost:5001`

### Unit Tests
- JUnit 5 with Mockito
- Spring Boot Test integration
- Test resources in `src/test/resources/`

## File Processing Flow

1. **Upload**: Files uploaded through web form (ADIF/CSV)
2. **Validation**: Comprehensive validation using `ValidatorService`
3. **Transformation**: Processing via `Adif3Transformer` from core library
4. **Output Generation**: Multiple formats (KML, ADIF, MD, TXT labels)
5. **Progress Tracking**: WebSocket-based progress updates
6. **Download**: Processed files available for download

## Template System

- **Thymeleaf** templates in `src/main/resources/templates/`
- Bootstrap 5 CSS framework
- FontAwesome icons
- Custom CSS with background image from amateur radio context

## Configuration Files

### Print Configurations
Multiple printer configurations in `src/main/resources/config/`:
- Various label sizes and formats
- UTF-8 and standard character encoding options
- Specialized formats for contests and awards

### ADIF Schema
- XML schema validation: `src/main/resources/adif/adx312generic.xsd`

## Dependencies and Integration

### Core Amateur Radio Library
- Primary dependency: `uk.m0nom:adif-processor:${project.version}`
- Contains core ADIF processing, coordinate conversion, activity databases

### Key Spring Boot Starters
- `spring-boot-starter-web` - Web MVC
- `spring-boot-starter-thymeleaf` - Template engine
- `spring-boot-starter-websocket` - Progress updates
- `spring-boot-starter-test` - Testing framework

### Database Support
- H2 (development/testing)
- PostgreSQL (production)
- JPA with custom repository scanning

## Development Notes

- **Lombok** is used throughout for reducing boilerplate code
- **WebSocket** integration for real-time processing progress
- **Swagger** API documentation available
- **File upload** handling uses modern Spring multipart support
- **Logging** configured with custom handlers and file output
- **Activity branch** contains new API development work

## Important Patterns

- Constructor-based dependency injection throughout
- Service layer separation between web and core processing
- Comprehensive validation before processing
- Progress callback patterns for long-running operations
- Environment-specific configuration handling
- Amateur radio domain expertise embedded in validation rules