package st.project.game.view;

import st.project.game.controller.SaveManager;
import st.project.game.controller.UserManager;
import st.project.game.model.GameModel;
import st.project.game.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainMenu extends JFrame {
    private User user;
    private UserManager userManager;
    private SaveManager saveManager;

    public MainMenu(User user, UserManager userManager) {
        this.user = user;
        this.userManager = userManager;
        this.saveManager = new SaveManager();
        initComponents();
    }

    private void initComponents() {
        setTitle("Menu Principal - " + user.getLogin());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(0x0D0D1A));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel welcome = new JLabel("Bem‑vindo, " + user.getLogin() + "!", SwingConstants.CENTER);
        welcome.setFont(new Font("Serif", Font.BOLD, 20));
        welcome.setForeground(new Color(0xF0C040));
        add(welcome, gbc);

        gbc.gridy = 1;
        JButton newGameBtn = createButton("Novo Jogo");
        newGameBtn.addActionListener(e -> startNewGame());
        add(newGameBtn, gbc);

        gbc.gridy = 2;
        JButton continueBtn = createButton("Continuar");
        continueBtn.addActionListener(e -> continueGame());
        add(continueBtn, gbc);

        gbc.gridy = 3;
        JButton rankingBtn = createButton("Ver Ranking");
        rankingBtn.addActionListener(e -> new RankingDialog(userManager.getAllUsers()));
        add(rankingBtn, gbc);

        if (userManager.isSuperUser(user.getLogin())) {
            gbc.gridy = 4;
            JButton manageBtn = createButton("Gerenciar Usuários");
            manageBtn.addActionListener(e -> manageUsers());
            add(manageBtn, gbc);
        }

        gbc.gridy = 5;
        JButton logoutBtn = createButton("Sair");
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginScreen();
        });
        add(logoutBtn, gbc);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startNewGame() {
        int slot = saveManager.getFreeSlot(user);
        if (slot == -1) {
            JOptionPane.showMessageDialog(this, "Todos os slots estão ocupados. Finalize um jogo primeiro.");
            return;
        }
        GameModel model = new GameModel();
        new GameGUI(model, user, userManager, saveManager, slot);
        this.dispose();
    }

    private void continueGame() {
        List<Integer> slots = saveManager.listSlots(user);
        if (slots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum jogo salvo encontrado.");
            return;
        }
        int slot;
        if (slots.size() == 1) {
            slot = slots.getFirst();
        } else {
            String[] options = slots.stream().map(i -> "Slot " + (i + 1)).toArray(String[]::new);
            String choice = (String) JOptionPane.showInputDialog(this, "Escolha o save:",
                    "Continuar Jogo", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (choice == null) return;
            int idx = choice.lastIndexOf(' ') + 1;
            slot = Integer.parseInt(choice.substring(idx)) - 1;
        }
        GameModel model = saveManager.loadGame(user, slot);
        if (model == null) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar o jogo.");
            return;
        }
        new GameGUI(model, user, userManager, saveManager, slot);
        this.dispose();
    }

    private void manageUsers() {
        JDialog dialog = new JDialog(this, "Gerenciar Usuários", true);
        dialog.setLayout(new BorderLayout());
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> userList = new JList<>(listModel);
        userManager.getAllUsers().forEach(u -> listModel.addElement(u.getLogin() + " (score: " + u.getBestScore() + ")"));
        JButton deleteBtn = new JButton("Excluir selecionado");
        deleteBtn.addActionListener(ev -> {
            String selected = userList.getSelectedValue();
            if (selected == null) return;
            String login = selected.split(" ")[0];
            if (login.equals("admin")) {
                JOptionPane.showMessageDialog(dialog, "Não é possível excluir o superusuário.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(dialog, "Excluir " + login + "?");
            if (confirm == JOptionPane.YES_OPTION) {
                userManager.deleteUser(login);
                listModel.removeElement(selected);
            }
        });
        dialog.add(new JScrollPane(userList), BorderLayout.CENTER);
        dialog.add(deleteBtn, BorderLayout.SOUTH);
        dialog.setSize(300, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(0x1C1C38));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }
}