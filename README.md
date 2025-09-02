# Tong - Anonymous Chat Platform

A comprehensive anonymous chat application with real-time messaging, group management, and both Java/JavaFX and React web interfaces.

## Features

### 🔐 Authentication & User Management
- **Anonymous Registration**: Automatically generates unique anonymous names and passwords
- **Login/Logout**: Secure session management
- **User Search**: Find other users by their anonymous names

### 💬 Chat Features
- **Private Messaging**: One-on-one chats with temporary or permanent options
- **Chat Requests**: Send and accept chat invitations
- **Real-time Messaging**: Instant message delivery
- **Message History**: Persistent chat history

### 👥 Group Management
- **Create Groups**: Temporary (24-hour) or permanent groups
- **Join Requests**: Request to join existing groups
- **Group Administration**: Full admin controls
- **Notice Board**: Group-wide announcements that only admins can post

### 🛡️ Administrative Features
- **Mute Users**: Temporarily silence disruptive members
- **Kick Users**: Remove users from groups
- **Make Admin**: Promote trusted members to admin status
- **Creator Privileges**: Group creators have ultimate control

## Architecture

### Server-Side (Java)
- **Enhanced Server**: Multi-threaded server handling multiple clients
- **User Management**: Complete user authentication and session management
- **Group Management**: Full group lifecycle and permission system
- **Data Persistence**: Automatic saving/loading of users, groups, and messages
- **Message Routing**: Intelligent message delivery to online users

### Client Applications

#### 1. JavaFX Desktop Client
- **Login Interface**: Modern login/registration form
- **Main Chat Dashboard**: Comprehensive chat interface with sidebar navigation
- **Real-time Updates**: Live message updates and notifications
- **Group Administration**: Full admin panel for group management

#### 2. React Web Client
- **Material-UI Design**: Modern, responsive web interface
- **Real-time WebSocket**: Live messaging with WebSocket simulation
- **Progressive Web App**: Can be installed as a desktop/mobile app
- **Cross-platform**: Works on all modern browsers

## Project Structure

```
Tong/
├── server/                     # Java Server Application
│   ├── src/
│   │   ├── App.java           # Server launcher with GUI
│   │   ├── models/            # Data models and server logic
│   │   │   ├── EnhancedServer.java      # Main server class
│   │   │   ├── ClientHandler.java       # Client connection handler
│   │   │   ├── User.java               # User data model
│   │   │   ├── ChatGroup.java          # Group data model
│   │   │   ├── Message.java            # Message data model
│   │   │   └── AnonymousNameGenerator.java # Name/ID generator
│   │   ├── controllers/       # JavaFX controllers
│   │   └── views/            # FXML UI files
│   └── lib/                  # JavaFX libraries
│
├── client/                    # JavaFX Desktop Client
│   ├── src/
│   │   ├── App.java          # Client launcher
│   │   ├── models/           # Client-side models
│   │   │   └── EnhancedClient.java     # Client connection class
│   │   ├── controllers/      # JavaFX controllers
│   │   │   ├── LoginController.java    # Login/register logic
│   │   │   └── MainChatController.java # Main chat interface
│   │   └── views/           # FXML UI files
│   │       ├── login_ui.fxml           # Login interface
│   │       └── main_chat_ui.fxml       # Main chat interface
│   └── lib/                 # JavaFX libraries
│
├── web-client/               # React Web Application
│   ├── src/
│   │   ├── App.js           # Main React application
│   │   ├── components/      # React components
│   │   │   ├── LoginPage.js          # Web login interface
│   │   │   ├── ChatDashboard.js      # Main chat dashboard
│   │   │   ├── ChatWindow.js         # Chat message interface
│   │   │   ├── UserSearch.js         # User search dialog
│   │   │   └── GroupManagement.js    # Group admin interface
│   │   └── services/        # API and WebSocket services
│   │       ├── WebSocketService.js   # Real-time messaging
│   │       └── ApiService.js         # HTTP API calls
│   └── package.json         # Node.js dependencies
│
└── group-chat/              # Legacy simple group chat
    └── src/
        ├── Server.java      # Basic server implementation
        ├── Client.java      # Basic client implementation
        └── ClientHandler.java # Basic client handler
```

## Getting Started

### Prerequisites
- Java 11+ with JavaFX
- Node.js 16+ (for web client)
- Git

### Running the Server

1. **Start the Enhanced Server:**
   ```bash
   cd server
   # Compile and run (Windows)
   javac --module-path lib --add-modules javafx.controls,javafx.fxml -d bin src/**/*.java
   java --module-path lib --add-modules javafx.controls,javafx.fxml -cp bin App
   ```

### Running the JavaFX Client

1. **Start the Desktop Client:**
   ```bash
   cd client
   # Compile and run (Windows)
   javac --module-path lib --add-modules javafx.controls,javafx.fxml -d bin src/**/*.java
   java --module-path lib --add-modules javafx.controls,javafx.fxml -cp bin App
   ```

### Running the Web Client

1. **Install Dependencies:**
   ```bash
   cd web-client
   npm install
   ```

2. **Start Development Server:**
   ```bash
   npm start
   ```

3. **Access the Application:**
   - Open browser to `http://localhost:3000`

## Usage Guide

### Getting Started
1. **Register**: Click "Get New Anonymous Account" to receive your credentials
2. **Login**: Use your anonymous name and password to login
3. **Search Users**: Find other users to chat with
4. **Send Chat Requests**: Send temporary or permanent chat invitations
5. **Create Groups**: Start your own group with admin privileges
6. **Chat**: Start messaging with individuals or groups

### Chat Types
- **Temporary Chats**: Auto-expire after a set time
- **Permanent Chats**: Remain until manually removed
- **Temporary Groups**: Auto-delete after 24 hours
- **Permanent Groups**: Remain indefinitely

### Admin Features
- **Mute**: Prevent users from sending messages
- **Kick**: Remove users from groups
- **Admin Promotion**: Give admin rights to trusted members
- **Notice Board**: Post group-wide announcements

## Protocol Documentation

### Message Format
All messages follow the format: `COMMAND:PARAMETER1:PARAMETER2:...`

### Client-to-Server Commands
```
REGISTER                              # Register new user
LOGIN:username:password               # Login with credentials
SEARCH_USERS:query                    # Search for users
SEND_CHAT_REQUEST:username:type       # Send chat request (type: temp/perm)
ACCEPT_CHAT_REQUEST:username:type     # Accept chat request
SEND_PRIVATE_MESSAGE:username:content # Send private message
CREATE_GROUP:name:type               # Create group (type: temp/perm)
JOIN_GROUP_REQUEST:groupId           # Request to join group
SEND_GROUP_MESSAGE:groupId:content   # Send group message
SEND_NOTICE:groupId:content          # Send admin notice
MUTE_USER:groupId:username           # Mute group member
KICK_USER:groupId:username           # Kick group member
MAKE_ADMIN:groupId:username          # Promote to admin
```

### Server-to-Client Responses
```
REGISTER_SUCCESS:username:password    # Registration successful
LOGIN_SUCCESS:sessionId              # Login successful
CHAT_REQUEST:username:type           # Incoming chat request
PRIVATE_MESSAGE:sender:content       # Incoming private message
GROUP_MESSAGE:groupId:sender:content # Incoming group message
NOTICE:groupId:sender:content        # Group notice
JOIN_REQUEST:groupId:username        # Group join request
MUTED:groupId:admin                  # You were muted
KICKED:groupId:admin                 # You were kicked
```

## Security Features

- **Session Management**: Secure session tokens for authentication
- **Input Validation**: Server-side validation of all inputs
- **Rate Limiting**: Prevents message spam and abuse
- **Admin Controls**: Comprehensive moderation tools
- **Data Persistence**: Automatic backup of user data and messages

## Development

### Adding New Features
1. Update the protocol in `ClientHandler.java`
2. Add corresponding methods in `EnhancedClient.java`
3. Update UI controllers to use new functionality
4. Test with both JavaFX and web clients

### Extending the Web Client
1. Add new components in `src/components/`
2. Update `WebSocketService.js` for new message types
3. Add new API endpoints in `ApiService.js`
4. Update the main dashboard to include new features

## Contributing

1. Fork the repository
2. Create a feature branch
3. Implement your changes
4. Test with both client types
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Future Enhancements

- **File Sharing**: Support for image and file transfers
- **Voice Chat**: Real-time voice communication
- **Mobile Apps**: Native iOS and Android applications
- **End-to-End Encryption**: Enhanced security for sensitive conversations
- **Database Integration**: Replace file-based storage with database
- **Load Balancing**: Support for multiple server instances
- **Push Notifications**: Background notifications for mobile devices
