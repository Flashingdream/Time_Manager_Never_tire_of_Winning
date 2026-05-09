# Time_Manager_Never_tire_of_Winning
This is the mission created for cugers who are remembers of Never tire of winning for the class of SEB

## Getting Started

### Prerequisites
- Node.js (for frontend)
- Java 11+ (for backend)
- Maven (for backend, or use Maven wrapper)

### Running the Application

#### Backend (Spring Boot)
Navigate to `demo/backend` directory:

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

**Windows:**
```powershell
# If Maven is installed
mvn spring-boot:run
# Or use Maven wrapper (requires shell environment)
./mvnw spring-boot:run
```

#### Frontend (Vue.js)
Navigate to `demo/frontend` directory:

```bash
npm install
npm run serve
```

The application will be available at:
- Frontend: http://localhost:8080
- Backend API: http://localhost:8080/api

### IntelliJ IDEA Support
This project can run directly in IntelliJ IDEA:

#### Backend (Spring Boot)
1. Open the `demo/backend` folder in IDEA
2. IDEA will automatically detect the Maven project
3. Right-click on `DemoApplication.java` and select "Run" or "Debug"

#### Frontend (Vue.js)
1. Ensure Node.js plugin is installed in IDEA
2. Open the `demo/frontend` folder
3. Right-click on `package.json` and select "Run 'npm install'"
4. Then run "Run 'npm run serve'"

For full-stack development, you may need to run frontend and backend separately, or configure IDEA run configurations for both.
