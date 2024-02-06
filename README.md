# SkyBin

SkyBin is a file sharing service accessed through the user's web browser. It consists of a Spring Boot backend and a React frontend. Interactions between the frontend and backend are done via a REST API and HTTP requests.

## Getting Started

To run the application, follow these steps:

1. Clone the repository.
2. Set up a local MySQL database named "Skybin".
3. Configure the database connection in the `src/main/resources/application.properties` file. **Note: You will need to change `spring.datasource.username` to your local MySQL username and `spring.datasource.password` to your local MySQL password.**
4. Build the project using Maven: `mvn clean install`.
5. Run the generated JAR file: `java -jar target/skybin-3.2.0.jar`.
6. Access the application in your web browser at `http://localhost:4000`.

## Features

- Upload and share files securely.
- Manage your files with ease.
- Secure user authentication.

## Technologies Used

- Spring Boot
- React
- REST API
- Maven

## Documentation

For detailed documentation, please refer to the [SkyBin Documentation](https://mliamsinclair.dev/assets/SkybinDocumentation-Llwm61M6.pdf).