# Tong - IUT Chat Application

A JavaFX-based real-time messaging application designed for university students to communicate through direct messages and group chats.

![Tong Logo](src/views/tongLogo.png)

## 🚀 Features

-   **User Authentication**: Secure login and registration system with email verification
-   **Direct Messaging**: Private one-on-one conversations
-   **Group Chat**: Multi-user chat rooms for discussions
-   **Real-time Communication**: Instant message delivery using socket programming
-   **Email Verification**: PIN-based email verification for account security
-   **User Profiles**: Customizable user profiles with display names
-   **Modern UI**: Clean and intuitive JavaFX interface

## � Screenshots

### Login Interface

![Login Screen](Screenshots/login.png)

### Registration Process

![Registration Form](Screenshots/register.png)

### Direct Messaging

![Direct Messages](Screenshots/directMessaging.png)

### Group Chat

![Group Chat](Screenshots/groupChat.png)

### Forum/Room Selection

![Forum Interface](Screenshots/forum.png)

## �🛠️ Technology Stack

-   **Frontend**: JavaFX with FXML
-   **Backend**: Java Socket Programming
-   **Database**: MySQL
-   **Authentication**: BCrypt password hashing
-   **Email Service**: JavaMail API with Gmail SMTP
-   **UI Animations**: AnimateFX library

## 📋 Prerequisites

Before running this application, make sure you have:

-   Java 11 or higher
-   MySQL Server
-   JavaFX Runtime (if not included in your JDK)
-   Maven or direct JAR dependencies

## 🔧 Setup Instructions

### 1. Database Setup

1. Install MySQL Server
2. Create a database named `tongchat`
3. Update database credentials in `src/database/DatabaseHelper.java`:
    ```java
    private static final String URL = "jdbc:mysql://localhost:3306/tongchat";
    private static final String USER = "your_username";
    private static final String PASSWORD = "your_password";
    ```

### 2. Email Configuration

1. Configure Gmail SMTP settings in `src/utils/EmailService.java`:
    ```java
    private static final String EMAIL_USERNAME = "your_email@gmail.com";
    private static final String EMAIL_PASSWORD = "your_app_password";
    ```
2. Enable 2-factor authentication on your Gmail account
3. Generate an App Password for the application

### 3. Dependencies

The following JAR files are required (included in the `lib` folder):

-   **JavaFX**: javafx.base.jar, javafx.controls.jar, javafx.fxml.jar, javafx.graphics.jar
-   **Database**: mysql-connector-j-9.4.0.jar
-   **Email**: jakarta.mail-1.6.3.jar, jakarta.activation-2.0.1.jar
-   **Security**: bcrypt-0.7.0.jar
-   **UI**: AnimateFX-1.3.0.jar
-   **Utilities**: gson-2.13.1.jar, commons-lang3-3.18.0.jar

### 4. Compilation and Execution

#### Using Command Line:

1. **Compile the application**:

    ```bash
    javac -cp "lib/*" -d bin src/**/*.java
    ```

2. **Run the Server**:

    ```bash
    java -cp "bin;lib/*" server.Server
    ```

3. **Run the Client Application**:
    ```bash
    java -cp "bin;lib/*" --module-path "lib" --add-modules javafx.controls,javafx.fxml App
    ```

#### Using VS Code:

1. Open the project in VS Code
2. Ensure the Java Extension Pack is installed
3. Run the server using the provided launch configuration
4. Run the client application

## 🏗️ Project Structure

```
Tong/
├── src/
│   ├── App.java                    # Main application entry point
│   ├── client/                     # Client-side networking
│   │   ├── ClientHandler.java
│   │   ├── DMClientHandler.java
│   │   ├── ForumClient.java
│   │   └── GCClientHandler.java
│   ├── controllers/                # JavaFX Controllers
│   │   ├── DMController.java
│   │   ├── GCController.java
│   │   ├── LoginController.java
│   │   ├── RegistrationController.java
│   │   └── RoomController.java
│   ├── database/                   # Database utilities
│   │   ├── DatabaseHelper.java
│   │   └── UserDAO.java
│   ├── models/                     # Data models
│   │   └── User.java
│   ├── server/                     # Server-side networking
│   │   ├── DMServer.java
│   │   ├── GCServer.java
│   │   └── Server.java
│   ├── utils/                      # Utility classes
│   │   └── EmailService.java
│   └── views/                      # FXML UI files
│       ├── dm.fxml
│       ├── gc.fxml
│       ├── login.fxml
│       ├── profile.fxml
│       ├── register.fxml
│       ├── room.fxml
│       └── tongLogo.png
├── Screenshots/                    # Application screenshots
├── lib/                            # External dependencies
└── bin/                            # Compiled classes
```

## 🎯 How to Use

### Registration

1. Launch the application
2. Click "Create Account" on the login screen
3. Fill in your details (Full Name, Display Name, Email)
4. Verify your email with the PIN sent to your inbox
5. Complete registration

### Login

1. Enter your registered email and password
2. Click "Sign In" to access the main chat interface

### Messaging

-   **Direct Messages**: Click on a user to start a private conversation
-   **Group Chats**: Join or create group chat rooms for multiple participants
-   **Real-time Updates**: Messages appear instantly for all connected users

## 🔒 Security Features

-   **Password Hashing**: Passwords are encrypted using BCrypt
-   **Email Verification**: PIN-based verification prevents unauthorized registrations
-   **Session Management**: Secure session handling for authenticated users
-   **Input Validation**: Comprehensive validation on both client and server sides

## 🛡️ Database Schema

The application uses the following main tables:

-   **Users**: Stores user account information
-   **Messages**: Stores chat messages with timestamps
-   **Rooms**: Manages chat room information
-   **Participants**: Links users to chat rooms

## 🚦 Server Architecture

The application uses a multi-threaded server architecture:

-   **Main Server**: Handles client connections on port 1234
-   **Client Handlers**: Individual threads for each connected client
-   **Message Broadcasting**: Real-time message distribution to relevant clients
-   **Separate Handlers**: Dedicated handlers for DM and GC functionalities

## 🎨 UI Components

The application features a modern JavaFX interface with:

-   **Login/Registration Forms**: Clean authentication interface
-   **Chat Windows**: Separate windows for direct messages and group chats
-   **User Profiles**: Customizable user information display
-   **Animations**: Smooth transitions using AnimateFX

## 📝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Note**: Remember to configure your database and email settings before running the application. Never commit sensitive credentials to version control.
