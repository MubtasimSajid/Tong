package models;

public class User {
    public int id;
    public String fullName;
    public String displayName;
    public String emailAddress;
    public String password;
    public String randomIdentifier;
    public String colorHex;

    public User() {}

    public User(String fullName, String displayName, String emailAddress, String password, String randomIdentifier) {
        this.fullName = fullName;
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.randomIdentifier = randomIdentifier;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return emailAddress;
    }

    public void setEmail(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRandomIdentifier() {
        return randomIdentifier;
    }

    public void setRandomIdentifier(String randomIdentifier) {
        this.randomIdentifier = randomIdentifier;
    }
}
