package st.project.game.view;

import st.project.game.controller.UserManager;
import st.project.game.model.User;

import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JFrame {
    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton loginButton, createAccountButton, rankingButton;
    private UserManager userManager;

    public LoginScreen() {
        userManager = new UserManager();
        initComponents();
    }

    private void initComponents() {
        setTitle("Cálice Sagrado - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(0x0D0D1A));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Aventura Mágica", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setForeground(new Color(0xF0C040));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        add(createLabel("Login:"), gbc);
        loginField = new JTextField(15);
        gbc.gridx = 1;
        add(loginField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        add(createLabel("Senha:"), gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        add(passwordField, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        buttonPanel.setOpaque(false);

        loginButton = createButton("Entrar");
        createAccountButton = createButton("Criar Conta");
        rankingButton = createButton("Ver Ranking");

        buttonPanel.add(loginButton);
        buttonPanel.add(createAccountButton);
        buttonPanel.add(rankingButton);

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        loginButton.addActionListener(e -> login());
        createAccountButton.addActionListener(e -> createAccount());
        rankingButton.addActionListener(e -> showRanking());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void login() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (login.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos.");
            return;
        }
        User user = userManager.authenticate(login, password);
        if (user != null) {
            this.dispose();
            new MainMenu(user, userManager);
        } else {
            JOptionPane.showMessageDialog(this, "Login ou senha incorretos.");
        }
    }

    private void createAccount() {
        JTextField loginF = new JTextField();
        JPasswordField passF = new JPasswordField();
        JTextField avatarF = new JTextField("avatar1.png");
        Object[] msg = {"Login:", loginF, "Senha:", passF, "Avatar:", avatarF};
        int option = JOptionPane.showConfirmDialog(this, msg, "Criar Conta", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String login = loginF.getText().trim();
            String password = new String(passF.getPassword());
            String avatar = avatarF.getText().trim();
            if (login.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Login e senha são obrigatórios.");
                return;
            }
            if (userManager.registerUser(login, password, avatar)) {
                JOptionPane.showMessageDialog(this, "Conta criada com sucesso!");
            } else {
                JOptionPane.showMessageDialog(this, "Login já existe. Escolha outro.");
            }
        }
    }

    private void showRanking() {
        new RankingDialog(userManager.getAllUsers());
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(0xE8E8F0));
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return label;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(0x1C1C38));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}