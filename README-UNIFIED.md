# 🚀 Tong Messenger - Unified Application

**The complete premium chat experience in a single application!**

A modern chat application that combines JavaFX frontend and Spring Boot backend into one unified, easy-to-use application. No more complex multi-module setups or separate deployments.

## ✨ Key Features

- **� Single Application** - Everything in one place, no modules to manage
- **🎨 Premium JavaFX UI** - Modern messenger-style interface with dark/light themes
- **🖥️ Spring Boot Backend** - RESTful API with WebSocket real-time messaging
- **💾 Embedded Database** - H2 in-memory database, no external setup required
- **🔐 Built-in Security** - Spring Security authentication system
- **� Multiple Launch Modes** - Backend only, client only, or both together
- **� Responsive Design** - Adaptive UI that works on different screen sizes

## 🏗️ Unified Project Structure

```
Tong/
├── src/main/java/com/tongchat/
│   ├── TongUnifiedApplication.java        # 🚀 Main unified launcher
│   ├── TongMessengerApplication.java      # 🖥️ Spring Boot backend
│   ├── backend/                           # Backend components
│   │   ├── config/                        # Spring configuration
│   │   ├── controller/                    # REST & WebSocket controllers  
│   │   ├── entity/                        # JPA entities
│   │   ├── repository/                    # Data repositories
│   │   └── service/                       # Business logic services
│   └── client/                            # Frontend components
│       ├── TongClientApplication.java     # 🎨 JavaFX main application
│       ├── controllers/                   # FXML controllers
│       └── models/                        # Client data models
├── src/main/resources/
│   ├── application.properties             # Backend configuration
│   └── com/tongchat/client/views/         # FXML layouts, CSS themes, images
├── pom.xml                                # Single unified Maven configuration
├── tong.bat                              # 🎯 Universal launcher script
└── README-UNIFIED.md                      # This file
```

## 🚦 Super Easy Launch

### Option 1: Universal Launcher (Recommended)
```bash
# Interactive mode with menu
./tong.bat

# Direct launch modes
./tong.bat backend    # Backend server only
./tong.bat client     # JavaFX client only  
./tong.bat both       # Complete system
./tong.bat help       # Show help
```

### Option 2: Maven Commands
```bash
# Build the project
mvn clean compile

# Launch in interactive mode
mvn spring-boot:run

# Launch JavaFX client only
mvn javafx:run

# Launch backend only
mvn spring-boot:run -Dspring-boot.run.mainClass=com.tongchat.TongMessengerApplication
```

### Option 3: Direct Java Execution
```bash
# Compile first
mvn compile

# Interactive launcher
java -cp target/classes com.tongchat.TongUnifiedApplication

# Specific modes
java -cp target/classes com.tongchat.TongUnifiedApplication backend
java -cp target/classes com.tongchat.TongUnifiedApplication client
java -cp target/classes com.tongchat.TongUnifiedApplication both
```

## 🎯 Launch Modes Explained

### 🖥️ Backend Mode
- Starts only the Spring Boot server
- Perfect for API development and testing
- Accessible at: `http://localhost:8081`
- WebSocket endpoint: `ws://localhost:8081/ws`
- H2 Console: `http://localhost:8081/h2-console`

### 🎨 Client Mode  
- Launches only the JavaFX premium UI
- Connects to running backend server
- Features dark/light theme switching
- Real-time chat with WebSocket connection

### 🚀 Both Mode
- Starts backend server first
- Automatically launches JavaFX client when ready
- Complete system experience
- Perfect for end-users

## 🎨 UI Highlights

- **Premium Design** - Modern messenger-style chat interface
- **Theme Switching** - Instant dark/light mode toggle
- **Real-time Chat** - WebSocket-powered instant messaging
- **User Management** - Anonymous user creation and profiles
- **Group Features** - Create and join chat groups
- **Responsive Layout** - Adapts to different window sizes

## 🖥️ Backend Capabilities

- **RESTful API** - Complete chat management endpoints
- **WebSocket Support** - Real-time bidirectional communication
- **JPA/Hibernate** - Object-relational mapping with H2 database
- **Spring Security** - Authentication and authorization
- **CORS Enabled** - Cross-origin resource sharing
- **Auto-configuration** - Zero external dependencies

## ⚡ Quick Start (30 seconds)

1. **Clone and navigate:**
   ```bash
   git clone <repository-url>
   cd Tong
   ```

2. **Launch complete system:**
   ```bash
   ./tong.bat both
   ```

3. **That's it!** Backend server starts, then JavaFX client launches automatically.

## 🔧 Configuration

### Backend Settings (`application.properties`)
```properties
# Server configuration
server.port=8081

# Database (H2 in-memory)
spring.datasource.url=jdbc:h2:mem:tongchatdb
spring.h2.console.enabled=true
spring.datasource.username=sa
spring.datasource.password=

# JPA settings
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

### UI Themes
- **Light Theme:** `src/main/resources/com/tongchat/client/views/light-theme.css`
- **Dark Theme:** `src/main/resources/com/tongchat/client/views/dark-theme.css`
- **Premium Styles:** `src/main/resources/com/tongchat/client/views/premium-style.css`

## 🚀 Deployment Options

### Single JAR Deployment
```bash
mvn clean package
java -jar target/tong-messenger-unified-1.0.0.jar [mode]
```

### Development Mode
```bash
# Auto-reload backend
mvn spring-boot:run

# Auto-reload client (in separate terminal)
mvn javafx:run
```

## 🔗 API Reference

| Endpoint | Method | Description |
|----------|---------|-------------|
| `/api/auth/register/anonymous` | POST | Create anonymous user |
| `/api/auth/user/{username}` | GET | Get user information |
| `/api/groups/create` | POST | Create new chat group |
| `/api/groups` | GET | List all available groups |
| `/ws` | WebSocket | Real-time messaging endpoint |

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report

# Integration tests
mvn verify
```

## � System Requirements

- **Java:** JDK 17 or higher
- **Maven:** 3.6+ 
- **JavaFX:** Included in dependencies
- **Memory:** 512MB RAM minimum
- **Storage:** 50MB for application + dependencies

## 🎯 Why Unified?

✅ **Simplicity** - One application, one POM, one repository
✅ **Easy Development** - No module dependencies to manage  
✅ **Quick Setup** - Single command to run everything
✅ **Easy Deployment** - One JAR file contains everything
✅ **Better Testing** - Test full stack integration easily
✅ **Reduced Complexity** - No inter-module communication issues

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**🚀 Tong Messenger - Where simplicity meets premium chat experience!**
