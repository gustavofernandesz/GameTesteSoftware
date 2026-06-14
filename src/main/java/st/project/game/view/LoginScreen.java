package st.project.game.view;

import st.project.game.controller.UserManager;
import st.project.game.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoginScreen extends JFrame {

    JTextField loginField;
    JPasswordField passwordField;

    private final UserManager userManager;

    /**
     * Construtor padrão (produção)
     */
    public LoginScreen() {
        this(new UserManager());
    }

    /**
     * Construtor injetável (testes)
     */
    public LoginScreen(UserManager userManager) {
        this.userManager = userManager;
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

        // ── Título ─────────────────────────────────────────

        JLabel title = new JLabel("Aventura Mágica", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setForeground(new Color(0xF0C040));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        add(title, gbc);

        // ── Login ──────────────────────────────────────────

        gbc.gridwidth = 1;

        gbc.gridy = 1;
        gbc.gridx = 0;

        add(createLabel("Login:"), gbc);

        loginField = new JTextField(15);
        loginField.setName("loginField");

        gbc.gridx = 1;

        add(loginField, gbc);

        // ── Senha ──────────────────────────────────────────

        gbc.gridy = 2;
        gbc.gridx = 0;

        add(createLabel("Senha:"), gbc);

        passwordField = new JPasswordField(15);
        passwordField.setName("passwordField");

        gbc.gridx = 1;

        add(passwordField, gbc);

        // ── Botões ─────────────────────────────────────────

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        buttonPanel.setOpaque(false);

        JButton loginButton = createButton("Entrar", "loginButton");
        JButton createAccountButton = createButton("Criar Conta", "createAccountButton");
        JButton rankingButton = createButton("Ver Ranking",  "rankingButton");

        buttonPanel.add(loginButton);
        buttonPanel.add(createAccountButton);
        buttonPanel.add(rankingButton);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        add(buttonPanel, gbc);

        // ── Eventos ────────────────────────────────────────

        loginButton.addActionListener(e -> login());

        createAccountButton.addActionListener(e -> createAccount());

        rankingButton.addActionListener(e -> showRanking());

        // ── Finalização ────────────────────────────────────

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Fluxo de login
     */
    protected void login() {

        String login = loginField.getText().trim();

        String password = new String(passwordField.getPassword());

        // validação

        if (login.isEmpty() || password.isEmpty()) {

            showMessage("Preencha todos os campos.");

            return;
        }

        User user = userManager.authenticate(login, password);

        // sucesso

        if (user != null) {

            this.dispose();

            openMainMenu(user);
        }

        // falha

        else {

            showMessage("Login ou senha incorretos.");
        }
    }

    /**
     * Fluxo de criação de conta
     */
    protected void createAccount() {

        JTextField loginF = new JTextField();
        loginF.setName("createAccountLoginField");

        JPasswordField passF = new JPasswordField();
        passF.setName("createAccountPasswordField");

        JTextField avatarF = new JTextField("avatar1.png");
        avatarF.setName("createAccountAvatarField");

        Object[] msg = {
                "Login:", loginF,
                "Senha:", passF,
                "Avatar:", avatarF
        };

        int option = showConfirmDialog(msg);

        // cancelado

        if (option != JOptionPane.OK_OPTION) {
            return;
        }

        String login = loginF.getText().trim();

        String password = new String(passF.getPassword());

        String avatar = avatarF.getText().trim();

        // validação

        if (login.isEmpty() || password.isEmpty()) {

            showMessage("Login e senha são obrigatórios.");

            return;
        }

        // sucesso

        if (userManager.registerUser(login, password, avatar)) {

            showMessage("Conta criada com sucesso!");
        }

        // falha

        else {

            showMessage("Login já existe. Escolha outro.");
        }
    }

    /**
     * Exibe ranking
     */
    protected void showRanking() {

        List<User> users = userManager.getAllUsers();

        openRanking(users);
    }

    /**
     * Wrapper testável do JOptionPane.showMessageDialog
     */
    protected void showMessage(String message) {

        JOptionPane.showMessageDialog(this, message);
    }

    /**
     * Wrapper testável do JOptionPane.showConfirmDialog
     */
    protected int showConfirmDialog(Object[] msg) {

        return JOptionPane.showConfirmDialog(
                this,
                msg,
                "Criar Conta",
                JOptionPane.OK_CANCEL_OPTION
        );
    }

    /**
     * Wrapper da navegação para MainMenu
     */
    protected void openMainMenu(User user) {

        new MainMenu(user, userManager);
    }

    /**
     * Wrapper da navegação para RankingDialog
     */
    protected void openRanking(List<User> users) {

        new RankingDialog(users);
    }

    /**
     * Cria labels padronizadas
     */
    protected JLabel createLabel(String text) {

        JLabel label = new JLabel(text);

        label.setForeground(new Color(0xE8E8F0));

        label.setFont(new Font("SansSerif", Font.PLAIN, 14));

        return label;
    }

    /**
     * Cria botões padronizados
     */
    protected JButton createButton(String text, String id) {

        JButton btn = new JButton(text);

        btn.setBackground(new Color(0x1C1C38));

        btn.setForeground(Color.WHITE);

        btn.setFocusPainted(false);

        btn.setName(id);

        return btn;
    }

    public static void main(String[] args) {

        try {

            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName()
            );

        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(LoginScreen::new);
    }
}