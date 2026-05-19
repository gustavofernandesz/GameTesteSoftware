package st.project.game.model;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String login;
    private String passwordHash;
    private String avatar;
    private int bestScore;
    private int totalSessions;

    public User(String login, String password, String avatar) {
        this.login = login;
        this.passwordHash = hashPassword(password);
        this.avatar = avatar;
        this.bestScore = 0;
        this.totalSessions = 0;
    }

    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public int getBestScore() { return bestScore; }
    public int getTotalSessions() { return totalSessions; }

    public void updateScore(int newScore) {
        if (newScore > bestScore) {
            bestScore = newScore;
        }
    }

    public void incrementSessions() {
        totalSessions++;
    }

    public boolean checkPassword(String password) {
        return hashPassword(password).equals(passwordHash);
    }

    public static String hashPassword(String password) {
        return hashWithAlgorithm(password, "SHA-256");
    }

    public static String hashWithAlgorithm(String password, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(algorithm + " não disponível", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return login.equals(user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return login + " (melhor: " + bestScore + ", sessões: " + totalSessions + ")";
    }
}