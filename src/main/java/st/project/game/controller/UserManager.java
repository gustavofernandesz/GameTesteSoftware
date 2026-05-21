package st.project.game.controller;

import st.project.game.model.User;

import java.io.*;
import java.util.*;

public class UserManager {
    private static final String USERS_FILE = "users.dat";
    private List<User> users;

    public UserManager() {
        loadUsers();
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            users = new ArrayList<>();
            users.add(new User("admin", "admin", "admin_avatar.png"));
            saveUsers();
        } else {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (List<User>) ois.readObject();
            } catch (Exception e) {
                users = new ArrayList<>();
                users.add(new User("admin", "admin", "admin_avatar.png"));
                saveUsers();
            }
        }
    }

    private void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean registerUser(String login, String password, String avatar) {
        if (getUser(login) != null) return false;
        users.add(new User(login, password, avatar));
        saveUsers();
        return true;
    }

    public User authenticate(String login, String password) {
        User u = getUser(login);
        return (u != null && u.checkPassword(password)) ? u : null;
    }

    public boolean deleteUser(String login) {
        User u = getUser(login);
        if (u == null || u.getLogin().equals("admin")) return false;
        users.remove(u);
        new SaveManager().deleteAllSaves(login);
        saveUsers();
        return true;
    }

    public User getUser(String login) {
        return users.stream().filter(u -> u.getLogin().equals(login)).findFirst().orElse(null);
    }

    public List<User> getAllUsers() {
        return Collections.unmodifiableList(users);
    }

    public boolean isSuperUser(String login) {
        return "admin".equals(login);
    }

    public void updateUserScoreAndSession(User user, int newScore) {
        User stored = getUser(user.getLogin());
        if (stored != null) {
            stored.updateScore(newScore);
            stored.incrementSessions();
            saveUsers();
        }
    }
}