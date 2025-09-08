# Tong Chat Application Configuration Template

## Database Configuration

Before running the application, update the database credentials in:
`src/database/DatabaseHelper.java`

```java
private static final String URL = "jdbc:mysql://localhost:3306/tongchat";
private static final String USER = "your_database_username";
private static final String PASSWORD = "your_database_password";
```

### Database Setup Steps:

1. Install MySQL Server
2. Create a new database:
    ```sql
    CREATE DATABASE tongchat;
    ```
3. Create necessary tables (tables will be created automatically by the application)

## Email Configuration

Update the email service credentials in:
`src/utils/EmailService.java`

```java
private static final String EMAIL_USERNAME = "your_email@gmail.com";
private static final String EMAIL_PASSWORD = "your_gmail_app_password";
```

### Gmail Setup Steps:

1. Enable 2-Factor Authentication on your Gmail account
2. Generate an App Password:
    - Go to Google Account Settings > Security > 2-Step Verification > App passwords
    - Select "Mail" and generate a 16-character password
    - Use this password in the EMAIL_PASSWORD field

## Server Configuration

The default server configuration uses:

-   **Port**: 1234
-   **Host**: localhost

To change these settings, modify the `Server.java` file.

## Security Notes

-   Never commit actual credentials to version control
-   Use environment variables or external configuration files for sensitive data
-   Ensure your database server is properly secured
-   Use strong passwords for both database and email accounts

## Troubleshooting

### Common Issues:

1. **Database Connection Failed**

    - Verify MySQL server is running
    - Check database credentials
    - Ensure the database exists

2. **Email Service Not Working**

    - Verify Gmail credentials
    - Check if 2FA is enabled
    - Ensure App Password is correctly generated

3. **JavaFX Runtime Issues**

    - Ensure JavaFX modules are in the module path
    - Verify JavaFX version compatibility

4. **Port Already in Use**
    - Check if another instance is running
    - Change the port number in Server.java if needed

## 🔒 Security Checklist

Before deploying or sharing this project, ensure you have:

### ✅ **Credentials Security**

-   [ ] Replaced all actual database credentials with placeholders
-   [ ] Removed actual email credentials and app passwords
-   [ ] No hardcoded passwords or API keys in source code
-   [ ] No sensitive information in comments

### ✅ **File Security**

-   [ ] Added comprehensive .gitignore file
-   [ ] Excluded IDE configuration files (.vscode, .idea)
-   [ ] Excluded compiled binaries and build artifacts
-   [ ] Included Screenshots/ folder while excluding sensitive images

### ✅ **Code Quality**

-   [ ] No TODO/FIXME comments with sensitive information
-   [ ] Proper exception handling without exposing sensitive data
-   [ ] Consistent coding standards throughout the project
-   [ ] Documentation is up-to-date and accurate

### ✅ **Production Readiness**

-   [ ] Environment-specific configuration separated from code
-   [ ] Logging levels appropriate for production
-   [ ] Database connection pooling considered for production
-   [ ] Input validation implemented properly

### ⚠️ **Before Going Public**

-   [ ] Review all commit history for accidentally committed secrets
-   [ ] Test the application with placeholder credentials
-   [ ] Verify all documentation is accurate
-   [ ] Consider adding integration tests

## 🔧 Development Environment Setup

### For Contributors:

1. **Clone the repository**
2. **Configure your local environment**:
    - Copy CONFIGURATION.md as your setup guide
    - Set up your local MySQL database
    - Configure your email service credentials
3. **Install dependencies** (JAR files are included in lib/)
4. **Test the application** before making changes
