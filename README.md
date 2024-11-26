## **Spring Boot Credit App**

### **Description**
This project is a simple Spring Boot-based application. It uses the H2 in-memory database and provides RESTful APIs.
The application is deployable using Docker, and database credentials (username and password) are passed as environment variables.

---

### **Features**
* Built with Spring Boot Framework
* H2 in-memory database
* RESTful APIs
* Dockerized for easy deployment
* Environment variable-based configuration

---


### **Setup and Usage**

#### **1. Prerequisites**
- Java 17 or later
- Maven 3 or later
- Docker

#### **2. Clone the Repository**

```bash
git clone https://github.com/barisakbayir/spring-boot-credit-app.git
cd spring-boot-credit-app
```

#### **3. Build with Maven**

```bash
mvn clean package
```

#### **4. Build Docker Image**

```bash
docker build -t spring-boot-credit-app .
```

#### **5. Run the Application Using Docker**

Run the application with the following command:

```bash
docker run -p 8090:8080 -e DB_USERNAME=your_db_username -e DB_PASSWORD=your_db_password spring-boot-credit-app
```

- The application will now be available at `http://localhost:8090`.

---
---

### **H2 Database Console**

You can access the H2 database console at:
- URL: `http://localhost:8090/h2-console`
- JDBC URL: `jdbc:h2:mem:creditdb`
- Username: **DB_USERNAME** (environment variable)
- Password: **DB_PASSWORD** (environment variable)


---

### **Asciidoc Documentation**

#### **How to Generate Asciidoc**

1. Run the following Maven command to generate the Asciidoc files:

```bash
mvn verify
```

2. The Asciidoc files will be generated in the `target/generated-docs` directory.
3. Open the generated `api-documentation.html` file in your browser to view the documentation.

---

### **Testing**

#### **Run Unit Tests**
To execute unit tests:

```bash
mvn test
```

---


### **Sample Data**
When the application starts, some sample customer data is preloaded into the database for testing purposes.

* some title

| ID | name     | surname | Credit Limit | Used Credit Limit |  
|----|----------|---------|--------------|-------------------|  
| 1  | John     | Doe     | 5000.0       | 0.0               |  
| 2  | Jack     | Smith   | 10_000.0     | 0.0               |
| 3  | Jennifer | Smith   | 13_000.0     | 0.0               |


---