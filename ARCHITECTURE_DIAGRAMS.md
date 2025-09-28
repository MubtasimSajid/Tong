# Tong Chat Application - System Architecture & Design Documentation

## Table of Contents

1. [System Architecture Overview](#system-architecture-overview)
2. [Authentication Flow](#authentication-flow)
3. [Class Diagram](#class-diagram)
4. [Direct Message Flow](#direct-message-flow)
5. [Group Chat Flow](#group-chat-flow)
6. [Database Schema](#database-schema)

---

## 1. System Architecture Overview

```mermaid
graph TB
    subgraph "Client Side"
        A[App.java - JavaFX Application]
        B[LoginController]
        C[RegistrationController]
        D[RoomController - Main Hub]
        E[DMController]
        F[GCController]
        G[ForumClient]
    end

    subgraph "Server Side"
        H[Main Server :1234]
        I[DM Server :2000+]
        J[GC Server :3000+]
        K[ClientHandler]
        L[DMClientHandler]
        M[GCClientHandler]
    end

    subgraph "Database Layer"
        N[DatabaseHelper]
        O[UserDAO]
        P[(MySQL Database)]
    end

    subgraph "Utility Layer"
        Q[EmailService]
        R[BCrypt Security]
    end

    A --> B
    A --> C
    B --> D
    C --> D
    D --> E
    D --> F
    D --> G

    G --> H
    G --> I
    G --> J

    H --> K
    I --> L
    J --> M

    B --> N
    C --> N
    D --> N
    N --> O
    O --> P

    C --> Q
    B --> R
    C --> R

    K -.->|Broadcast| K
    L -.->|DM Messages| L
    M -.->|GC Messages| M
```

### Architecture Components:

#### **Client Side (JavaFX)**

-   **App.java**: Main application entry point, launches login window
-   **Controllers**: Handle UI logic and user interactions
    -   **LoginController**: Authentication interface
    -   **RegistrationController**: User registration with email verification
    -   **RoomController**: Main chat hub, manages DM/GC sessions
    -   **DMController**: Direct message interface
    -   **GCController**: Group chat interface
-   **ForumClient**: Socket client for server communication

#### **Server Side (Multi-threaded)**

-   **Main Server (Port 1234)**: Handles general forum communications
-   **DM Servers (Port 2000+)**: Dynamic servers for each DM session
-   **GC Servers (Port 3000+)**: Dynamic servers for each group chat
-   **Client Handlers**: Process client connections and messages

#### **Data Layer**

-   **DatabaseHelper**: MySQL connection management
-   **UserDAO**: User database operations
-   **MySQL Database**: Persistent data storage

#### **Utility Layer**

-   **EmailService**: Gmail SMTP for email verification
-   **BCrypt**: Password hashing and security

---

## 2. Authentication Flow

```mermaid
sequenceDiagram
    participant U as User
    participant LC as LoginController
    participant RC as RegistrationController
    participant ES as EmailService
    participant DAO as UserDAO
    participant DB as Database
    participant BCrypt as BCrypt

    Note over U,BCrypt: User Registration Flow
    U->>RC: Enter registration details
    RC->>ES: Generate and send PIN
    ES-->>U: Email with PIN
    U->>RC: Enter PIN
    RC->>RC: Validate PIN
    RC->>BCrypt: Hash password
    BCrypt-->>RC: Hashed password
    RC->>DAO: Save user data
    DAO->>DB: INSERT user record
    DB-->>DAO: Success confirmation
    DAO-->>RC: User created
    RC-->>U: Registration successful

    Note over U,BCrypt: User Login Flow
    U->>LC: Enter email/password
    LC->>DAO: Get user by email
    DAO->>DB: SELECT user
    DB-->>DAO: User data
    DAO-->>LC: User object
    LC->>BCrypt: Verify password
    BCrypt-->>LC: Password valid
    LC->>LC: Create user session
    LC-->>U: Login successful → RoomController
```

### Authentication Features:

-   **Email Verification**: PIN-based verification using Gmail SMTP
-   **Password Security**: BCrypt hashing with salt
-   **Session Management**: User session maintained across controllers
-   **Database Validation**: Secure user lookup and validation

---

## 3. Class Diagram

```mermaid
classDiagram
    class App {
        +start(Stage) void
        +main(String[]) void
    }

    class User {
        +int id
        +String fullName
        +String displayName
        +String emailAddress
        +String password
        +String randomIdentifier
        +String colorHex
        +getId() int
        +setId(int) void
        +getFullName() String
        +setFullName(String) void
        +getDisplayName() String
        +setDisplayName(String) void
        +getEmail() String
        +setEmail(String) void
        +getPassword() String
        +setPassword(String) void
        +getRandomIdentifier() String
        +setRandomIdentifier(String) void
    }

    class LoginController {
        -Button createAccountBtn
        -Button forgotPasswordBtn
        -PasswordField pwf_password
        -TextField tf_email
        +initialize() void
        +signIn() void
        +createAccount() void
        +forgotPassword() void
    }

    class RegistrationController {
        -TextField tf_fullName
        -TextField tf_displayName
        -TextField tf_email
        -PasswordField pwf_password
        -TextField tf_pin
        +initialize() void
        +register() void
        +sendPin() void
        +verifyPin() void
    }

    class RoomController {
        -User currentUser
        -ForumClient forumClient
        -boolean isConnectedToServer
        -User currentDMUser
        -String currentDMKey
        -Map~String,User~ pendingDMRequests
        -Map~String,RoomController~ activeControllers
        -Map~String,String~ savedDMConnections
        -Map~String,String~ savedGCConnections
        -Map~String,List~Node~~ chatHistories
        -String currentChatKey
        -chatType ct
        +setCurrentUser(User) void
        +sendMessage() void
        +getRandID() void
        +setRandID() void
        +editDisplayName() void
        +joinGC() void
        +enterDM() void
        +receiveDMMessage(User,String,String) void
        +showDMRequest(User,String) void
        +cleanup() void
    }

    class DMController {
        -Button button_send
        -TextField tf_message
        -ScrollPane sp_main
        -VBox vbox_messages
        -Label headLabel
        -User currentUser
        -User targetUser
        -ForumClient forumClient
        -boolean isConnectedToServer
        -String currentDMKey
        -DMServer currentDMServer
        +initialize() void
        +setCurrentUser(User) void
        +setDMInfo(User,String,DMServer) void
        +setDMInfoForJoining(User,String,String,int) void
        +sendMessage() void
        -connectToDMServer() void
        -connectToExistingDMServer(String,int) void
    }

    class GCController {
        -Button button_send
        -TextField tf_message
        -ScrollPane sp_main
        -VBox vbox_messages
        -Label headLabel
        -User currentUser
        -ForumClient forumClient
        -boolean isConnectedToServer
        -String currentGCKey
        -String currentGCName
        -GCServer currentGCServer
        +initialize() void
        +setCurrentUser(User) void
        +setGCInfo(String,String,GCServer) void
        +setGCInfoForJoining(String,String,String,int) void
        +sendMessage() void
        -connectToGCServer() void
        -connectToExistingGCServer(String,int) void
    }

    class Server {
        +ServerSocket serverSocket
        +int port
        +Server(ServerSocket,int)
        +Server(ServerSocket)
        +startServer() void
        +closeServerSocket() void
        +main(String[]) void
    }

    class ClientHandler {
        +ArrayList~ClientHandler~ clientHandlers
        -Socket socket
        -BufferedReader bufferedReader
        -BufferedWriter bufferedWriter
        -String clientUsername
        +ClientHandler(Socket)
        +run() void
        +broadcastMessage(String) void
        +removeClientHandler() void
        +closeEverything(Socket,BufferedReader,BufferedWriter) void
    }

    class DMServer {
        -ServerSocket serverSocket
        -String dmKey
        -int port
        -boolean isRunning
        +DMServer(String,int)
        +startDMServer() void
        +stopDMServer() void
        +getPort() int
        +createDMServer(String) DMServer
        +getDMServer(String) DMServer
    }

    class GCServer {
        -ServerSocket serverSocket
        -String gcKey
        -String gcName
        -int port
        -boolean isRunning
        +GCServer(String,String,int)
        +startGCServer() void
        +stopGCServer() void
        +getPort() int
        +createGCServer(String,String) GCServer
        +getGCServer(String) GCServer
    }

    class ForumClient {
        -Socket socket
        -BufferedReader bufferedReader
        -BufferedWriter bufferedWriter
        -String username
        -ForumMessageListener messageListener
        +ForumClient(Socket,String)
        +sendMessage(String) void
        +listenForMessage() void
        +closeEverything(Socket,BufferedReader,BufferedWriter) void
        +setMessageListener(ForumMessageListener) void
    }

    class DatabaseHelper {
        -String URL
        -String USER
        -String PASSWORD
        +getConnection() Connection
    }

    class UserDAO {
        +createUser(User) boolean
        +getUserByEmail(String) User
        +getUserByRandomID(String) User
        +updateUser(User) boolean
        +deleteUser(int) boolean
    }

    class EmailService {
        -String SMTP_HOST
        -String SMTP_PORT
        -String EMAIL_USERNAME
        -String EMAIL_PASSWORD
        -Map~String,PinData~ pinStorage
        +sendPin(String) String
        +validatePin(String,String) boolean
        +cleanupExpiredPins() void
    }

    %% Relationships
    App --> LoginController
    App --> RegistrationController
    LoginController --> RoomController : navigates to
    RegistrationController --> RoomController : navigates to
    RoomController --> DMController : creates
    RoomController --> GCController : creates
    RoomController --> ForumClient : uses
    DMController --> ForumClient : uses
    GCController --> ForumClient : uses

    LoginController --> User : authenticates
    RegistrationController --> User : creates
    RoomController --> User : manages
    DMController --> User : communicates with
    GCController --> User : communicates with

    Server --> ClientHandler : creates
    DMServer --> ClientHandler : creates
    GCServer --> ClientHandler : creates

    ForumClient --> Server : connects to
    ForumClient --> DMServer : connects to
    ForumClient --> GCServer : connects to

    LoginController --> DatabaseHelper : uses
    RegistrationController --> DatabaseHelper : uses
    UserDAO --> DatabaseHelper : uses
    DatabaseHelper --> UserDAO : provides connection

    RegistrationController --> EmailService : uses
    LoginController --> EmailService : uses (forgot password)

    RoomController --> DMServer : creates/manages
    RoomController --> GCServer : creates/manages
```

### Key Class Relationships:

-   **MVC Pattern**: Controllers manage UI and business logic
-   **Client-Server**: ForumClient communicates with multiple server types
-   **Data Access**: DAO pattern for database operations
-   **Utility Services**: EmailService and DatabaseHelper provide shared functionality

---

## 4. Direct Message Flow

```mermaid
sequenceDiagram
    participant U1 as User 1 (Requester)
    participant RC1 as RoomController 1
    participant U2 as User 2 (Target)
    participant RC2 as RoomController 2
    participant DS as DM Server
    participant DB as Database

    Note over U1,DB: DM Request Initiation
    U1->>RC1: Click "Enter DM"
    RC1->>RC1: Show DM dialog
    U1->>RC1: Enter target Random ID
    RC1->>DB: Validate target user exists
    DB-->>RC1: Target user found
    RC1->>RC1: Create DM key (sorted IDs)
    RC1->>DS: Create DM Server
    DS-->>RC1: DM Server created on port
    RC1->>RC2: Send DM request

    Note over U1,DB: DM Request Handling
    RC2->>U2: Show DM request dialog
    alt Accept Request
        U2->>RC2: Accept DM request
        RC2->>RC1: Send acceptance
        RC1->>RC1: Load DMController
        RC1->>DS: Connect to DM Server
        RC2->>RC2: Load DMController
        RC2->>DS: Connect to DM Server

        Note over U1,DB: DM Communication Active
        U1->>RC1: Type and send message
        RC1->>DS: Send message to server
        DS->>RC2: Broadcast to connected clients
        RC2-->>U2: Display received message

        U2->>RC2: Type and send reply
        RC2->>DS: Send message to server
        DS->>RC1: Broadcast to connected clients
        RC1-->>U1: Display received message

    else Decline Request
        U2->>RC2: Decline DM request
        RC2->>RC1: Send decline notification
        RC1-->>U1: Show "Request declined"
        RC1->>DS: Stop DM Server
    end
```

### Direct Message Process:

1. **Request Initiation**: User enters target's Random ID
2. **Validation**: System verifies target user exists
3. **Server Creation**: Unique DM server created for conversation
4. **Request Notification**: Target user receives DM request
5. **Acceptance/Decline**: Target user chooses to accept or decline
6. **Connection**: Both users connect to dedicated DM server
7. **Real-time Communication**: Messages exchanged through DM server

---

## 5. Group Chat Flow

```mermaid
sequenceDiagram
    participant U1 as User 1 (Creator)
    participant RC1 as RoomController 1
    participant U2 as User 2 (Joiner)
    participant RC2 as RoomController 2
    participant U3 as User 3 (Joiner)
    participant RC3 as RoomController 3
    participant GCS as GC Server

    Note over U1,GCS: Group Chat Creation
    U1->>RC1: Click "Join GC"
    RC1->>RC1: Show GC dialog
    U1->>RC1: Enter GC name, select "Create New"
    RC1->>GCS: Create GC Server
    GCS-->>RC1: GC Server created on port
    RC1->>RC1: Load GCController
    RC1->>GCS: Connect as first member
    GCS-->>RC1: Connection established

    Note over U1,GCS: Users Joining Existing GC
    U2->>RC2: Click "Join GC"
    RC2->>RC2: Show GC dialog
    U2->>RC2: Enter IP, port, GC name
    RC2->>GCS: Attempt connection
    GCS-->>RC2: Connection accepted
    RC2->>RC2: Load GCController
    GCS->>RC1: Notify "User 2 joined"
    RC1-->>U1: Display join notification

    U3->>RC3: Join GC (same process)
    RC3->>GCS: Connect to GC
    GCS-->>RC3: Connection accepted
    GCS->>RC1: Notify "User 3 joined"
    GCS->>RC2: Notify "User 3 joined"

    Note over U1,GCS: Group Communication
    U1->>RC1: Send message
    RC1->>GCS: Message to server
    GCS->>RC2: Broadcast to User 2
    GCS->>RC3: Broadcast to User 3
    RC2-->>U2: Display message
    RC3-->>U3: Display message

    U2->>RC2: Send reply
    RC2->>GCS: Message to server
    GCS->>RC1: Broadcast to User 1
    GCS->>RC3: Broadcast to User 3
    RC1-->>U1: Display message
    RC3-->>U3: Display message

    Note over U1,GCS: User Leaving
    U3->>RC3: Close GC window
    RC3->>GCS: Disconnect
    GCS->>RC1: Notify "User 3 left"
    GCS->>RC2: Notify "User 3 left"
    RC1-->>U1: Display leave notification
    RC2-->>U2: Display leave notification
```

### Group Chat Features:

-   **Dynamic Server Creation**: Each GC gets its own server instance
-   **Multi-user Support**: Unlimited users can join a group chat
-   **Real-time Broadcasting**: Messages instantly delivered to all members
-   **Join/Leave Notifications**: Users notified when others join or leave
-   **Persistent Sessions**: GC servers remain active while users are connected

---

## 6. Database Schema

```mermaid
erDiagram
    USERS {
        int id PK
        string full_name
        string display_name
        string email_address UK
        string password_hash
        string random_identifier UK
        string color_hex
        timestamp created_at
        timestamp updated_at
    }

    CHAT_ROOMS {
        int room_id PK
        string room_name
        string room_type
        string room_key UK
        string creator_id FK
        timestamp created_at
        boolean is_active
    }

    MESSAGES {
        int message_id PK
        int room_id FK
        int sender_id FK
        text message_content
        timestamp sent_at
        string message_type
        boolean is_deleted
    }

    ROOM_PARTICIPANTS {
        int participation_id PK
        int room_id FK
        int user_id FK
        timestamp joined_at
        timestamp left_at
        boolean is_active
    }

    USER_SESSIONS {
        int session_id PK
        int user_id FK
        string session_token UK
        timestamp created_at
        timestamp expires_at
        boolean is_active
    }

    DM_CONNECTIONS {
        int dm_id PK
        int user1_id FK
        int user2_id FK
        string dm_key UK
        timestamp created_at
        boolean is_active
    }

    EMAIL_VERIFICATIONS {
        int verification_id PK
        string email_address
        string pin_code
        timestamp created_at
        timestamp expires_at
        boolean is_verified
    }

    %% Relationships
    USERS ||--o{ MESSAGES : sends
    USERS ||--o{ CHAT_ROOMS : creates
    USERS ||--o{ ROOM_PARTICIPANTS : participates
    USERS ||--o{ USER_SESSIONS : has
    USERS ||--o{ DM_CONNECTIONS : "user1"
    USERS ||--o{ DM_CONNECTIONS : "user2"

    CHAT_ROOMS ||--o{ MESSAGES : contains
    CHAT_ROOMS ||--o{ ROOM_PARTICIPANTS : has

    MESSAGES }o--|| ROOM_PARTICIPANTS : "sent in"
```

### Database Schema Details:

#### **USERS Table**

-   **Primary Data**: User account information
-   **Security**: Hashed passwords, unique random identifiers
-   **Customization**: Display names, color preferences

#### **CHAT_ROOMS Table**

-   **Room Management**: Stores DM and GC room information
-   **Types**: "DM" for direct messages, "GC" for group chats
-   **Keys**: Unique room identifiers for server mapping

#### **MESSAGES Table**

-   **Message Storage**: All chat messages with metadata
-   **Relationships**: Links to users and rooms
-   **Features**: Soft delete, message types

#### **ROOM_PARTICIPANTS Table**

-   **Membership Tracking**: User participation in rooms
-   **History**: Join/leave timestamps
-   **Status**: Active participation status

#### **DM_CONNECTIONS Table**

-   **Direct Message Mapping**: Links two users in DM
-   **Unique Keys**: Prevents duplicate DM sessions
-   **Status Tracking**: Active/inactive DM connections

---

## 7. Technology Stack Deep Dive

### Frontend Architecture

-   **JavaFX**: Modern UI framework with FXML layouts
-   **FXML Controllers**: MVC pattern implementation
-   **Scene Management**: Dynamic window creation for chats
-   **AnimateFX**: Smooth UI transitions and animations

### Backend Architecture

-   **Socket Programming**: TCP connections for real-time communication
-   **Multi-threading**: Concurrent client handling
-   **Dynamic Server Creation**: On-demand DM/GC servers
-   **Connection Pooling**: Efficient resource management

### Security Implementation

-   **BCrypt**: Industry-standard password hashing
-   **Email Verification**: PIN-based account validation
-   **Session Management**: Secure user session handling
-   **Input Validation**: Comprehensive data sanitization

### Database Design

-   **MySQL**: Reliable relational database
-   **Connection Pooling**: Efficient database access
-   **DAO Pattern**: Clean data access layer
-   **Prepared Statements**: SQL injection prevention

---

## 8. Communication Protocols

### Message Format

```json
{
	"type": "message|notification|system",
	"sender": "username",
	"content": "message content",
	"timestamp": "2024-01-01T12:00:00Z",
	"room_id": "chat_room_identifier"
}
```

### Server Communication

-   **Port 1234**: Main forum server
-   **Port 2000+**: Dynamic DM servers
-   **Port 3000+**: Dynamic GC servers
-   **Protocol**: TCP socket connections
-   **Encoding**: UTF-8 text messages

---
