
# Task Vision 🚀
**Task Vision** is a Java backend-based **Project Management Hub** application designed to help teams manage tasks, improve collaboration, and manage time more optimally.

---

## ✨ Main Features
- 🔑 OAuth JWT (JSON Web Token)
- 📌 Manage Task (Create, Read, Update, Delete)
- 🧾 Export Report Features With Excel
- ⬆️ Features KPI
- 🔄 Report Tasks With Feedback
- 🔥 Validation Data With Spring Validation

---

## 🛠️ Tech Stack
| Layer        | Teknologi            |
|--------------|----------------------|
| Backend      | Java 17, Spring Boot |
| Database     | MySQL                |
| Security     | JWT Authentication   |
| ORM          | Spring Data JPA      |
| Testing      | JUnit + Mockito      |

---

## 📌 Architecture
```
Controller → Service → Repository → Entity
                 ↓
         JWT Security + AOP
```
---

## 🔑 Installation
1. Clone this repository:
```bash
git clone https://github.com/M3Rabbany/Task-Vision.git
cd Task-Vision
```
2. Create File **application.properties** di `src/main/resources`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/task_vision
spring.datasource.username=root
spring.datasource.password=your_password
jwt.secret=your_secret_key
```
3. Running App:
```bash
mvn spring-boot:run
```

---

## 📌 API Documentations
| Endpoint             | Method | Description                          |
|----------------------|--------|--------------------------------------|
| `/api/auth/register` | POST   | Register User                        |
| `/api/auth/login`    | POST   | Login User                           |
| `/api/tasks`         | GET    | Get All Tasks                        |
| `/api/projects`      | POST   | Create Projects                      |
| `/api/reports`       | GET    | Get Total Report With Template Excel |
| `/api/users`         | GET    | Users Profile                        |

---

## 📄 License
This project is intended for learning and personal development purposes.

---
