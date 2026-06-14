package st.project.game.view;

import st.project.game.controller.GameEngine;
import st.project.game.controller.SaveManager;
import st.project.game.controller.UserManager;
import st.project.game.model.GameModel;
import st.project.game.model.Room;
import st.project.game.model.User;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.Stack;

public class GameGUI extends JFrame implements PropertyChangeListener {

    private static final Color BG_DARK       = new Color(0x0D0D1A);
    private static final Color BG_PANEL      = new Color(0x14142B);
    private static final Color BG_CARD       = new Color(0x1C1C38);
    private static final Color ACCENT_GOLD   = new Color(0xF0C040);
    private static final Color ACCENT_PURPLE = new Color(0x8B5CF6);
    private static final Color ACCENT_TEAL   = new Color(0x2DD4BF);
    private static final Color TEXT_LIGHT    = new Color(0xE8E8F0);
    private static final Color TEXT_DIM      = new Color(0x7878A0);
    private static final Color TILE_VISITED  = new Color(0x00FF80);
    private static final Color TILE_LOCKED   = new Color(0x1A1A2E);
    private static final Color TILE_NORMAL   = new Color(0x1E1E3C);
    private static final Color TILE_PLAYER   = new Color(0x2E4A2E);
    private static final Color TILE_BORDER   = new Color(0x3A3A60);
    private static final Color PATH_COLOR    = new Color(0x60, 0x60, 0xB0);


    private static final Color A1_NORMAL_C1  = new Color(0x1E1E3C);
    private static final Color A1_NORMAL_C2  = new Color(0x26264E);
    private static final Color A1_VISITED_C1 = new Color(0x00FF80);
    private static final Color A1_VISITED_C2 = new Color(0x32325A);
    private static final Color A1_PATH       = new Color(0x60, 0x60, 0xB0);

    // Andar 2: rosa
    private static final Color A2_NORMAL_C1  = new Color(0x2E1A2E);
    private static final Color A2_NORMAL_C2  = new Color(0x4A2645);
    private static final Color A2_VISITED_C1 = new Color(0xFF66CC);
    private static final Color A2_VISITED_C2 = new Color(0x5A2050);
    private static final Color A2_PATH       = new Color(0xB0, 0x50, 0xA0);

    // Andar 3: amarelo/âmbar
    private static final Color A3_NORMAL_C1  = new Color(0x28220A);
    private static final Color A3_NORMAL_C2  = new Color(0x3C3410);
    private static final Color A3_VISITED_C1 = new Color(0xFFD700);
    private static final Color A3_VISITED_C2 = new Color(0x4A3C00);
    private static final Color A3_PATH       = new Color(0xA0, 0x88, 0x00);

    // Andar 4: vermelho
    private static final Color A4_NORMAL_C1  = new Color(0x2E0808);
    private static final Color A4_NORMAL_C2  = new Color(0x4A1010);
    private static final Color A4_VISITED_C1 = new Color(0xFF4444);
    private static final Color A4_VISITED_C2 = new Color(0x5A0C0C);
    private static final Color A4_PATH       = new Color(0xB0, 0x20, 0x20);

    private static final int TILE_SIZE = 100;

    private final GameModel model;
    private final GameEngine engine;
    private final JPanel     mapPanel;

    private JLabel timeLabel;
    private JLabel statusLabel;
    private JLabel movesLabel;
    private JLabel levelLabel;   // novo
    private JLabel scoreLabel;   // novo

    private JLabel andarLabel;         // andar atual

    private final JTextArea logArea;

    private Font fontTitle;
    private Font fontMono;
    private Font fontBody;

    // Novos campos para gerenciamento
    private User user;
    private UserManager userManager;
    private SaveManager saveManager;
    private int slot;

    public GameGUI(GameModel model, User user, UserManager userManager, SaveManager saveManager, int slot) {
        this.model = model;
        this.user = user;
        this.userManager = userManager;
        this.saveManager = saveManager;
        this.slot = slot;

        loadFonts();

        setTitle("Aventura Mágica - Missão: Cálice Sagrado");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                engine.pausar();
                // Salva o jogo antes de fechar
                if (!engine.isJogoEncerrado()) {
                    saveManager.saveGame(model, user, slot);
                }
                dispose();
                new MainMenu(user, userManager);
            }
        });
        setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        engine = new GameEngine(model);
        model.addPropertyChangeListener(this);

        JPanel topPanel = buildTopPanel();
        add(topPanel, BorderLayout.NORTH);

        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                desenharMapa((Graphics2D) g);
            }
        };
        mapPanel.setPreferredSize(new Dimension(5 * TILE_SIZE + 40, 5 * TILE_SIZE + 40));
        mapPanel.setBackground(BG_DARK);
        mapPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(BG_DARK);
        centerWrapper.add(mapPanel);
        add(centerWrapper, BorderLayout.CENTER);

        logArea = new JTextArea();
        JPanel rightPanel = buildRightPanel();
        add(rightPanel, BorderLayout.EAST);

        bindKeys();

        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Exibe estado inicial
        timeLabel.setText("Tempo: " + model.getTempoRestante() + "s");
        movesLabel.setText("Mov: " + model.getMovimentosRestantes());
        levelLabel.setText("Nível: " + model.getNivel());
        scoreLabel.setText("Score: " + model.getScore());

        //Adição de inicialização do andarLabel no construtor

        andarLabel.setText("Andar: " + model.getAndarAtual() + "/4");
        log("Bem-vindo, aventureiro!");
        log("  Encontre o Cálice Mágico.");
        log("  Você precisa da Chave Encantada");
        log("  para entrar na sala do cálice.");
        log("─────────────────────────────");
        log("  Mova-se: WASD ou setas");
        log("─────────────────────────────");
        atualizarMapa();
    }

    private JPanel buildTopPanel() {
        JPanel top = new JPanel(new GridLayout(1, 7));
        top.setBackground(BG_PANEL);
        top.setBorder(new MatteBorder(0, 0, 2, 0, ACCENT_GOLD));
        top.setPreferredSize(new Dimension(0, 52));

        timeLabel   = makeLabel("Tempo: 120s",         fontTitle, ACCENT_GOLD);
        movesLabel  = makeLabel("Mov: 20",             fontTitle, ACCENT_PURPLE);
        levelLabel  = makeLabel("Nível: 1",            fontBody,  ACCENT_TEAL);
        scoreLabel  = makeLabel("Score: 0",            fontBody,  ACCENT_GOLD);
        andarLabel  = makeLabel("Andar: 1/4",          fontTitle, new Color(0xFF8C00));
        JLabel title = makeLabel("CÁLICE SAGRADO",     fontTitle, TEXT_LIGHT);
        statusLabel  = makeLabel("Explorando...",      fontBody,  ACCENT_TEAL);

        top.add(wrapCenter(timeLabel));
        top.add(wrapCenter(movesLabel));
        top.add(wrapCenter(levelLabel));
        top.add(wrapCenter(scoreLabel));
        top.add(wrapCenter(andarLabel));
        top.add(wrapCenter(title));
        top.add(wrapCenter(statusLabel));
        return top;
    }

    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setBackground(BG_PANEL);
        right.setBorder(new CompoundBorder(
                new MatteBorder(0, 2, 0, 0, ACCENT_GOLD),
                new EmptyBorder(12, 12, 12, 12)
        ));
        right.setPreferredSize(new Dimension(230, 0));

        logArea.setEditable(false);
        logArea.setBackground(BG_CARD);
        logArea.setForeground(TEXT_LIGHT);
        logArea.setFont(fontMono);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        logArea.setCaretColor(ACCENT_PURPLE);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(TILE_BORDER));
        scroll.setBackground(BG_CARD);
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        right.add(scroll, BorderLayout.CENTER);

        right.add(buildDPad(), BorderLayout.SOUTH);
        return right;
    }

    private JPanel buildDPad() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setBackground(BG_PANEL);

        JLabel hint = makeLabel("[ WASD / setas ]", fontMono.deriveFont(10f), TEXT_DIM);
        wrapper.add(hint, BorderLayout.NORTH);

        JPanel dpad = new JPanel(new GridBagLayout());
        dpad.setBackground(BG_PANEL);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);

        c.gridx = 1; c.gridy = 0; dpad.add(dirBtn("^ Norte",  "norte"), c);
        c.gridx = 0; c.gridy = 1; dpad.add(dirBtn("< Oeste",  "oeste"), c);
        c.gridx = 1; c.gridy = 1; dpad.add(dirBtn("*",        null),    c);
        c.gridx = 2; c.gridy = 1; dpad.add(dirBtn("Leste >",  "leste"), c);
        c.gridx = 1; c.gridy = 2; dpad.add(dirBtn("v Sul",    "sul"),   c);

        wrapper.add(dpad, BorderLayout.CENTER);
        return wrapper;
    }

    private JButton dirBtn(String label, String direcao) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                boolean press = getModel().isPressed();
                Color base = press ? ACCENT_PURPLE.darker() : hover ? ACCENT_PURPLE : BG_CARD;
                Color bord = hover ? ACCENT_GOLD : TILE_BORDER;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(bord);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(hover ? Color.WHITE : TEXT_LIGHT);
                g2.setFont(fontMono.deriveFont(11f));
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth()  - fm.stringWidth(label)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(label, tx, ty);
                g2.dispose();
            }
        };
        btn.setName(Objects.requireNonNullElse(direcao, "nulo"));
        btn.setPreferredSize(new Dimension(72, 30));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusable(false);
        btn.setCursor(direcao != null
                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                : Cursor.getDefaultCursor());
        if (direcao != null) {
            btn.addActionListener(e -> mover(direcao));
        } else {
            btn.setEnabled(false);
        }
        return btn;
    }

    private void bindKeys() {
        InputMap  im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        Object[][] binds = {
                { KeyEvent.VK_UP,    "norte" }, { KeyEvent.VK_W, "norte" },
                { KeyEvent.VK_DOWN,  "sul"   }, { KeyEvent.VK_S, "sul"   },
                { KeyEvent.VK_LEFT,  "oeste" }, { KeyEvent.VK_A, "oeste" },
                { KeyEvent.VK_RIGHT, "leste" }, { KeyEvent.VK_D, "leste" },
        };

        for (Object[] b : binds) {
            int    key = (int)    b[0];
            String dir = (String) b[1];
            String id  = "move_" + key;
            im.put(KeyStroke.getKeyStroke(key, 0), id);
            am.put(id, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { mover(dir); }
            });
        }
    }

    public void mover(String direcao) {
        if (!model.isJogoAtivo()) return;
        boolean moveu = engine.mover(direcao);
        if (moveu) {
            // Só salva se o jogo ainda não foi encerrado pelo movimento acima
            if (!engine.isJogoEncerrado()) {
                saveManager.saveGame(model, user, slot);
            }
            atualizarMapa();
            Room atual = model.getJogador().getPosicaoAtual();
            log("-> " + atual.getNome());
            if (!model.getJogador().getInventario().isEmpty()) {
                log("  Inv: " + model.getJogador().getInventario());
            }
        } else {
            log("X Bloqueado ao " + direcao + ".");
        }
    }

    public void atualizarMapa() {
        mapPanel.repaint();
    }

    private void desenharMapa(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int pad = 20;

        Stack<Room> historico = model.getJogador().getHistorico();
        java.util.Set<Room> visitadas = new java.util.HashSet<>(historico);

        // São desenhadas as salas do andar atual do jogador.
        // O caminho (path) também é filtrado para mostrar apenas movimentos dentro do andar.
        // As cores de tiles e trilha variam conforme o andar (via getFloorColors/getFloorPathColor).

        int andarParaPath = model.getAndarAtual();
        Color pathColor = getFloorPathColor(model.getAndarAtual());
        if (historico.size() > 1) {
            g.setColor(pathColor);
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1f, new float[]{6, 4}, 0));
            Room anterior = null;
            for (Room room : historico) {
                if (anterior != null
                        && anterior.getAndar() == andarParaPath
                        && room.getAndar()     == andarParaPath) {
                    int x1 = anterior.getX() * TILE_SIZE + pad + TILE_SIZE / 2;
                    int y1 = anterior.getY() * TILE_SIZE + pad + TILE_SIZE / 2;
                    int x2 = room.getX()     * TILE_SIZE + pad + TILE_SIZE / 2;
                    int y2 = room.getY()     * TILE_SIZE + pad + TILE_SIZE / 2;
                    g.drawLine(x1, y1, x2, y2);
                }
                anterior = room;
            }
            g.setStroke(new BasicStroke(1));
        }

        int andarAtual = model.getAndarAtual();
        boolean chaveVisivel = model.isChaveVisivel();
        Room salaDaChave = model.getSalaDaChave();

        for (Room room : model.getSalas().values()) {
            // Só desenha salas do andar atual
            if (room.getAndar() != andarAtual) continue;

            int     rx       = room.getX() * TILE_SIZE + pad;
            int     ry       = room.getY() * TILE_SIZE + pad;
            boolean isPlayer = room == model.getJogador().getPosicaoAtual();
            boolean visited  = visitadas.contains(room);

            Color[] floorColors = getFloorColors(andarAtual);
            // floorColors: [0]=normal_c1 [1]=normal_c2 [2]=visited_c1 [3]=visited_c2
            if (isPlayer) {
                drawGradientRect(g, rx, ry, new Color(0x1A3A1A), TILE_PLAYER);
            } else if (room.isBloqueada()) {
                drawGradientRect(g, rx, ry, TILE_LOCKED, new Color(0x22224A));
            } else if (visited) {
                drawGradientRect(g, rx, ry, floorColors[2], floorColors[3]);
            } else {
                drawGradientRect(g, rx, ry, floorColors[0], floorColors[1]);
            }

            if (isPlayer) {
                g.setColor(ACCENT_TEAL);
                g.setStroke(new BasicStroke(2f));
            } else if (room.isBloqueada()) {
                g.setColor(new Color(0x444466));
                g.setStroke(new BasicStroke(1f));
            } else {
                g.setColor(TILE_BORDER);
                g.setStroke(new BasicStroke(1f));
            }
            g.drawRoundRect(rx, ry, TILE_SIZE - 1, TILE_SIZE - 1, 6, 6);
            g.setStroke(new BasicStroke(1));

            if (room.isEscada() && !isPlayer) {
                g.setColor(new Color(0xFF8C00, true));
                g.setStroke(new BasicStroke(2f));
                g.drawRoundRect(rx + 2, ry + 2, TILE_SIZE - 5, TILE_SIZE - 5, 4, 4);
                g.setStroke(new BasicStroke(1));
            }

            String label;
            if (room.isEscadaCima()) {
                label = "▲ SOBE";
            } else if (room.isEscadaBaixo()) {
                label = "▼ DESCE";
            } else {
                String n = room.getNome();
                label = n.length() > 10 ? n.substring(0, 10) : n;
            }
            g.setFont(fontMono.deriveFont(room.isEscada() ? 13f : 16f));
            g.setColor(room.isEscadaCima()  ? new Color(0xFF8C00) :
                    room.isEscadaBaixo() ? new Color(0xFF8C00) :
                            isPlayer ? ACCENT_TEAL : room.isBloqueada() ? TEXT_DIM : TEXT_LIGHT);
            drawStringCentered(g, label, rx, ry, 20);

            if (room.isBloqueada()) {
                drawLockIcon(g, rx + TILE_SIZE / 2, ry + TILE_SIZE / 2 + 8);
            }

            if (temItenVisivelNaSala(room, chaveVisivel, salaDaChave)) {
                drawItemGem(g, rx + TILE_SIZE - 18, ry + 5);
            }

            if (isPlayer) {
                drawPlayerIcon(g, rx + TILE_SIZE / 2, ry + TILE_SIZE / 2 + 8);
            }
        }

        if (model.isChaveAtiva()) {
            Room caliceRoom = model.getMissao().getSalaCalice();
            if (caliceRoom.getAndar() == andarAtual) {
                int rx = caliceRoom.getX() * TILE_SIZE + pad;
                int ry = caliceRoom.getY() * TILE_SIZE + pad;
                g.setColor(new Color(0xFF, 0xFF, 0x00, 60));
                g.fillRoundRect(rx, ry, TILE_SIZE, TILE_SIZE, 6, 6);
                g.setColor(ACCENT_GOLD);
                g.setStroke(new BasicStroke(2.5f));
                g.drawRoundRect(rx, ry, TILE_SIZE - 1, TILE_SIZE - 1, 6, 6);
                g.setStroke(new BasicStroke(1));
                g.setFont(fontBody.deriveFont(Font.BOLD, 11f));
                g.setColor(ACCENT_GOLD);
                drawStringCentered(g, "CÁLICE", rx, ry, TILE_SIZE - 6);
            }
        }
    }


    // - Se o item É a lupa → bolinha sempre visível.
    // - Se o item NÃO é a lupa e o jogador NÃO tem a lupa → bolinha NÃO aparece.
    // - Se o item NÃO é a lupa e o jogador JÁ tem a lupa → bolinha aparece normalmente.

    private boolean temItenVisivelNaSala(Room room, boolean chaveVisivel, Room salaDaChave) {
        for (st.project.game.model.Item item : room.getItems()) {
            if (item.getTipo() == st.project.game.model.Item.Type.LUPA) {
                return true; // lupa sempre visível
            }
            // [ALTERAÇÃO] Qualquer item que não é a lupa só aparece se o jogador já tem a lupa
            if (chaveVisivel) return true;
        }
        return false;
    }

    // Auxiliares que retornam as cores corretas de tile e trilha para cada andar,
    // centralizando a lógica de paleta e evitando condicionais espalhadas em desenharMapa.

    private Color[] getFloorColors(int andar) {
        return switch (andar) {
            case 2 -> new Color[]{A2_NORMAL_C1, A2_NORMAL_C2, A2_VISITED_C1, A2_VISITED_C2};
            case 3 -> new Color[]{A3_NORMAL_C1, A3_NORMAL_C2, A3_VISITED_C1, A3_VISITED_C2};
            case 4 -> new Color[]{A4_NORMAL_C1, A4_NORMAL_C2, A4_VISITED_C1, A4_VISITED_C2};
            default -> new Color[]{A1_NORMAL_C1, A1_NORMAL_C2, A1_VISITED_C1, A1_VISITED_C2};
        };
    }

    private Color getFloorPathColor(int andar) {
        return switch (andar) {
            case 2 -> A2_PATH;
            case 3 -> A3_PATH;
            case 4 -> A4_PATH;
            default -> A1_PATH;
        };
    }

    private void drawGradientRect(Graphics2D g, int x, int y, Color c1, Color c2) {
        GradientPaint gp = new GradientPaint(x, y, c1, x + TILE_SIZE, y + TILE_SIZE, c2);
        g.setPaint(gp);
        g.fillRoundRect(x, y, TILE_SIZE, TILE_SIZE, 6, 6);
        g.setPaint(null);
    }

    private void drawStringCentered(Graphics2D g, String s, int rx, int ry, int offsetY) {
        FontMetrics fm = g.getFontMetrics();
        int tx = rx + (TILE_SIZE - fm.stringWidth(s)) / 2;
        g.drawString(s, tx, ry + offsetY);
    }

    private void drawLockIcon(Graphics2D g, int cx, int cy) {
        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(0xFFAA00));
        g.drawArc(cx - 5, cy - 14, 10, 10, 0, 180);
        g.setColor(new Color(0xCC8800));
        g.fillRoundRect(cx - 7, cy - 7, 14, 11, 3, 3);
        g.setColor(new Color(0xFFAA00));
        g.drawRoundRect(cx - 7, cy - 7, 14, 11, 3, 3);
        g.setColor(BG_DARK);
        g.fillOval(cx - 2, cy - 4, 5, 5);
        g.setStroke(new BasicStroke(1));
    }

    private void drawItemGem(Graphics2D g, int x, int y) {
        g.setColor(new Color(0xFF, 0x00, 0xFF, 60));
        g.fillOval(x - 2, y - 2, 12 + 4, 12 + 4);
        GradientPaint gp = new GradientPaint(x, y, new Color(0xFF66FF), x + 12, y + 12, new Color(0x9900CC));
        g.setPaint(gp);
        g.fillOval(x, y, 12, 12);
        g.setPaint(null);
        g.setColor(new Color(0xFF99FF));
        g.setStroke(new BasicStroke(0.8f));
        g.drawOval(x, y, 12, 12);
        g.setStroke(new BasicStroke(1));
    }

    private void drawPlayerIcon(Graphics2D g, int cx, int cy) {
        for (int i = 3; i >= 0; i--) {
            g.setColor(new Color(0x00, 0xFF, 0x88, 12 * (4 - i)));
            g.fillOval(cx - 10 - i * 2, cy - 10 - i * 2, 20 + i * 4, 20 + i * 4);
        }
        g.setColor(ACCENT_TEAL);
        g.fillOval(cx - 7, cy - 7, 14, 14);
        g.setColor(new Color(0x00FFCC));
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(cx - 7, cy - 7, 14, 14);
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.WHITE);
        g.fillOval(cx - 2, cy - 2, 5, 5);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        switch (prop) {
            case "tempo":
                int seg = (int) evt.getNewValue();
                Color corTempo = seg <= 10 ? new Color(0xFF4444)
                        : seg <= 20 ? new Color(0xFFAA00)
                        : ACCENT_GOLD;
                timeLabel.setForeground(corTempo);
                timeLabel.setText("Tempo: " + seg + "s");
                break;

            case "movimentos":
                int mov = (int) evt.getNewValue();
                Color corMov = mov <= 3 ? new Color(0xFF4444)
                        : mov <= 5 ? new Color(0xFFAA00)
                        : ACCENT_PURPLE;
                movesLabel.setForeground(corMov);
                movesLabel.setText("Mov: " + mov);
                break;

            case "score":
                scoreLabel.setText("Score: " + evt.getNewValue());
                break;

            case "nivel":
                levelLabel.setText("Nível: " + evt.getNewValue());
                break;

            // "andar": atualiza o andarLabel e redesenha o mapa ao trocar de andar via escada.
            // "lupaObtida": exibe mensagem no log informando que a Chave foi revelada.

            case "andar":
                int novoAndar = (int) evt.getNewValue();
                andarLabel.setText("Andar: " + novoAndar + "/4");
                log("── Andar " + novoAndar + " ──");
                atualizarMapa();
                break;

            case "lupaObtida":
                // [ALTERAÇÃO] Mensagem atualizada: agora TODOS os itens são revelados pela lupa
                log("✦ Lupa obtida! Todos os itens ocultos foram revelados.");
                atualizarMapa();
                break;

            case "gameOver":
                boolean vitoria = (boolean) evt.getNewValue();
                if (vitoria) {
                    statusLabel.setForeground(ACCENT_GOLD);
                    statusLabel.setText("VITORIA! Missão cumprida!");
                    log("═════════════════════════════");
                    log("  PARABÉNS, AVENTUREIRO!");
                    log("  Você encontrou o Cálice!");
                    log("═════════════════════════════");
                    JOptionPane.showMessageDialog(this,
                            "Você venceu! Missão cumprida!",
                            "VITÓRIA", JOptionPane.INFORMATION_MESSAGE);
                    // Atualiza usuário e remove save
                    userManager.updateUserScoreAndSession(user, model.getScore());
                    System.out.println(saveManager.deleteSave(user, slot));
                } else {
                    statusLabel.setForeground(new Color(0xFF4444));
                    statusLabel.setText("Tempo esgotado - Fim de Jogo");
                    log("═════════════════════════════");
                    log("  TEMPO ESGOTADO");
                    log("  A missão fracassou...");
                    log("═════════════════════════════");
                    JOptionPane.showMessageDialog(this,
                            "Tempo esgotado! Fim de jogo.",
                            "FIM DE JOGO", JOptionPane.WARNING_MESSAGE);
                    // Em caso de derrota, também remove o save
                    saveManager.deleteSave(user, slot);
                }
                engine.encerrarJogo();
                // Fecha a janela e volta ao menu
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new MainMenu(user, userManager);
                });
                break;
        }
    }

    public void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(font);
        l.setForeground(color);
        l.setOpaque(false);
        return l;
    }

    private JPanel wrapCenter(JComponent c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_PANEL);
        p.add(c);
        return p;
    }

    private void loadFonts() {
        try {
            fontTitle = new Font("Palatino Linotype", Font.BOLD, 15);
            if (!fontTitle.getFamily().equals("Palatino Linotype")) throw new Exception();
        } catch (Exception e) {
            fontTitle = new Font("Serif", Font.BOLD, 15);
        }
        try {
            fontMono = new Font("Cascadia Code", Font.PLAIN, 12);
            if (!fontMono.getFamily().equals("Cascadia Code")) throw new Exception();
        } catch (Exception e) {
            fontMono = new Font("Monospaced", Font.PLAIN, 12);
        }
        fontBody = new Font("SansSerif", Font.PLAIN, 12);
    }

    private static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(0x44447A);
            trackColor = new Color(0x1C1C38);
        }
        @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
        private JButton zeroBtn() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }
}