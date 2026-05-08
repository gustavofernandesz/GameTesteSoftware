package st.project.game.controller;

import st.project.game.model.GameModel;
import st.project.game.model.User;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SaveManager {
    private static final String SAVE_DIR = "saves";
    private static final int MAX_SAVES_PER_USER = 3;

    public SaveManager() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();
    }

    private String getSaveFileName(String login, int slot) {
        return SAVE_DIR + File.separator + "save_" + login + "_" + slot + ".ser";
    }

    public void saveGame(GameModel model, User user, int slot) {
        String path = getSaveFileName(user.getLogin(), slot);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(model);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar jogo", e);
        }
    }

    public GameModel loadGame(User user, int slot) {
        String path = getSaveFileName(user.getLogin(), slot);
        File file = new File(path);
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (GameModel) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar jogo", e);
        }
    }

    public boolean deleteSave(User user, int slot) {
        String path = getSaveFileName(user.getLogin(), slot);
        try {
            return Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            return false;
        }
    }

    public List<Integer> listSlots(User user) {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < MAX_SAVES_PER_USER; i++) {
            if (new File(getSaveFileName(user.getLogin(), i)).exists()) {
                slots.add(i);
            }
        }
        return slots;
    }

    public int getFreeSlot(User user) {
        for (int i = 0; i < MAX_SAVES_PER_USER; i++) {
            if (!new File(getSaveFileName(user.getLogin(), i)).exists()) {
                return i;
            }
        }
        return -1;
    }

    public void deleteAllSaves(String login) {
        for (int i = 0; i < MAX_SAVES_PER_USER; i++) {
            new File(getSaveFileName(login, i)).delete();
        }
    }
}