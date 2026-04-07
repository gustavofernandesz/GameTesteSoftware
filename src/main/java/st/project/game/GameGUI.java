package st.project.game;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

public class GameGUI extends JFrame implements GameEngine.TimerListener {

    // ── Paleta dark-fantasy ──────────────────────────────────────────────────
    private static final Color BG_DARK       = new Color(0x0D0D1A);
    private static final Color BG_PANEL      = new Color(0x14142B);
    private static final Color BG_CARD       = new Color(0x1C1C38);
    private static final Color ACCENT_GOLD   = new Color(0xF0C040);
    private static final Color ACCENT_PURPLE = new Color(0x8B5CF6);
    private static final Color ACCENT_TEAL   = new Color(0x2DD4BF);
    private static final Color TEXT_LIGHT    = new Color(0xE8E8F0);
    private static final Color TEXT_DIM      = new Color(0x7878A0);
    private static final Color TILE_VISITED  = new Color(0x00FF80); // FIX #7: agora utilizado
    private static final Color TILE_LOCKED   = new Color(0x1A1A2E);
    private static final Color TILE_NORMAL   = new Color(0x1E1E3C);
    private static final Color TILE_PLAYER   = new Color(0x2E4A2E);
    private static final Color TILE_BORDER   = new Color(0x3A3A60);
    // FIX #6: Color(int, boolean=true) com 0x6060B0 teria alpha=0x00 (invisível).
    // Corrigido para construtor RGB explícito.
    private static final Color PATH_COLOR    = new Color(0x60, 0x60, 0xB0);


    private static final int TILE_SIZE = 100;

    private final GameEngine engine;
    private final JPanel     mapPanel;

    // FIX #1: timeLabel e statusLabel declarados como campos e atribuídos
    // diretamente em buildTopPanel(), eliminando o cast frágil via getComponent()
    // que causava ClassCastException.
    private JLabel timeLabel;
    private JLabel statusLabel;
    private JLabel movesLabel;

    private final JTextArea logArea;

    // ── Fontes ───────────────────────────────────────────────────────────────
    private Font fontTitle;
    private Font fontMono;
    private Font fontBody;

    public GameGUI() {
        loadFonts();

        setTitle("Aventura Magica - Missao: Calice Sagrado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 0));

        engine = new GameEngine(this);

        // ── Barra superior ──────────────────────────────────────────────────
        // FIX #1: buildTopPanel() agora atribui timeLabel e statusLabel diretamente.
        JPanel topPanel = buildTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // ── Mapa ────────────────────────────────────────────────────────────
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

        // ── Painel lateral ──────────────────────────────────────────────────
        logArea = new JTextArea();
        JPanel rightPanel = buildRightPanel();
        add(rightPanel, BorderLayout.EAST);

        // ── Teclado: setas + WASD ────────────────────────────────────────────
        bindKeys();

        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        log("Bem-vindo, aventureiro!");
        log("  Encontre o Calice Magico.");
        log("  Voce precisa da Chave Encantada");
        log("  para entrar na sala do calice.");
        log("─────────────────────────────");
        log("  Mova-se: WASD ou setas");
        log("─────────────────────────────");
        atualizarMapa();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Construção da UI
    // ─────────────────────────────────────────────────────────────────────────

    private JPanel buildTopPanel() {
        JPanel top = new JPanel(new GridLayout(1, 3));
        top.setBackground(BG_PANEL);
        top.setBorder(new MatteBorder(0, 0, 2, 0, ACCENT_GOLD));
        top.setPreferredSize(new Dimension(0, 52));

        // FIX #1: atribui diretamente aos campos em vez de recuperar via getComponent()
        timeLabel   = makeLabel("Tempo: 60s",          fontTitle, ACCENT_GOLD);
        JLabel title = makeLabel("CALICE SAGRADO",      fontTitle, TEXT_LIGHT);
        statusLabel  = makeLabel("Explorando...",       fontBody,  ACCENT_TEAL);
        movesLabel   = makeLabel("Mov: 7",         fontTitle, ACCENT_PURPLE);


        top.add(wrapCenter(timeLabel));
        top.add(wrapCenter(title));
        top.add(wrapCenter(statusLabel));
        top.add(wrapCenter(movesLabel));
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

        JLabel hint = makeLabel("[ WASD / Botões ]", fontMono.deriveFont(10f), TEXT_DIM);
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

    // ─────────────────────────────────────────────────────────────────────────
    // Key bindings (WASD + setas)
    // ─────────────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────────────
    // Lógica de movimento
    // ─────────────────────────────────────────────────────────────────────────

    public void mover(String direcao) {
        if (!engine.isJogoAtivo()) return;
        boolean moveu = engine.moverJogador(direcao);
        if (moveu) {
            atualizarMapa();
            Room atual = engine.getJogador().getPosicaoAtual();
            log("-> " + atual.getNome());
            if (!engine.getJogador().getInventario().isEmpty()) {
                log("  Inv: " + engine.getJogador().getInventario());
            }
        } else {
            log("X Bloqueado ao " + direcao + ".");
        }
    }

    public void atualizarMapa() {
        mapPanel.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Desenho do mapa
    // ─────────────────────────────────────────────────────────────────────────

    private void desenharMapa(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int pad = 20;

        // FIX #7: conjunto de salas visitadas para colorir com TILE_VISITED
        Stack<Room> historico = engine.getJogador().getHistorico();
        java.util.Set<Room> visitadas = new java.util.HashSet<>(historico);

        // Trilha percorrida — FIX #6: usa PATH_COLOR corrigido
        if (historico.size() > 1) {
            g.setColor(PATH_COLOR);
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1f, new float[]{6, 4}, 0));
            Room anterior = null;
            for (Room room : historico) {
                if (anterior != null) {
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

        // Tiles
        for (Room room : engine.getSalas().values()) {
            int     rx       = room.getX() * TILE_SIZE + pad;
            int     ry       = room.getY() * TILE_SIZE + pad;
            boolean isPlayer = room == engine.getJogador().getPosicaoAtual();
            boolean visited  = visitadas.contains(room);

            // Fundo — FIX #7: salas visitadas agora usam TILE_VISITED
            if (isPlayer) {
                drawGradientRect(g, rx, ry,
                        new Color(0x1A3A1A), TILE_PLAYER);
            } else if (room.isBloqueada()) {
                drawGradientRect(g, rx, ry,
                        TILE_LOCKED, new Color(0x22224A));
            } else if (visited) {
                drawGradientRect(g, rx, ry,
                        TILE_VISITED, new Color(0x32325A));
            } else {
                drawGradientRect(g, rx, ry,
                        TILE_NORMAL, new Color(0x26264E));
            }

            // Borda
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

            // Nome da sala (abreviado)
            String nome = room.getNome().length() > 10
                    ? room.getNome().substring(0, 10)
                    : room.getNome();
            g.setFont(fontMono.deriveFont(16f));
            g.setColor(isPlayer ? ACCENT_TEAL : room.isBloqueada() ? TEXT_DIM : TEXT_LIGHT);
            drawStringCentered(g, nome, rx, ry, 20);

            // FIX #9: cadeado desenhado geometricamente — sem dependência de suporte a emoji
            if (room.isBloqueada()) {
                drawLockIcon(g, rx + TILE_SIZE / 2, ry + TILE_SIZE / 2 + 8);
            }

            // Ícone de item
            if (!room.getItems().isEmpty()) {
                drawItemGem(g, rx + TILE_SIZE - 18, ry + 5);
            }

            // Ícone do jogador
            if (isPlayer) {
                drawPlayerIcon(g, rx + TILE_SIZE / 2, ry + TILE_SIZE / 2 + 8);
            }
        }

        // Destaque do cálice (amuleto ativo)
        if (engine.isChaveAtiva()) {
            Room caliceRoom = engine.getMissao().getSalaCalice();
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
            drawStringCentered(g, "CALICE", rx, ry, TILE_SIZE - 6);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitários de desenho
    // ─────────────────────────────────────────────────────────────────────────

    private void drawGradientRect(Graphics2D g, int x, int y, Color c1, Color c2) {
        GradientPaint gp = new GradientPaint(x, y, c1, x + GameGUI.TILE_SIZE, y + GameGUI.TILE_SIZE, c2);
        g.setPaint(gp);
        g.fillRoundRect(x, y, GameGUI.TILE_SIZE, GameGUI.TILE_SIZE, 6, 6);
        g.setPaint(null);
    }

    private void drawStringCentered(Graphics2D g, String s, int rx, int ry, int offsetY) {
        FontMetrics fm = g.getFontMetrics();
        int tx = rx + (GameGUI.TILE_SIZE - fm.stringWidth(s)) / 2;
        g.drawString(s, tx, ry + offsetY);
    }

    // FIX #9: cadeado desenhado com formas geométricas puras — funciona em qualquer JVM/SO.
    private void drawLockIcon(Graphics2D g, int cx, int cy) {
        g.setStroke(new BasicStroke(2f));
        // argola
        g.setColor(new Color(0xFFAA00));
        g.drawArc(cx - 5, cy - 14, 10, 10, 0, 180);
        // corpo
        g.setColor(new Color(0xCC8800));
        g.fillRoundRect(cx - 7, cy - 7, 14, 11, 3, 3);
        g.setColor(new Color(0xFFAA00));
        g.drawRoundRect(cx - 7, cy - 7, 14, 11, 3, 3);
        // buraco da fechadura
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

    // ─────────────────────────────────────────────────────────────────────────
    // Callbacks do engine
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void onTempoAtualizado(int segundosRestantes) {
        Color cor = segundosRestantes <= 10 ? new Color(0xFF4444)
                : segundosRestantes <= 20 ? new Color(0xFFAA00)
                : ACCENT_GOLD;
        timeLabel.setForeground(cor);
        timeLabel.setText("Tempo: " + segundosRestantes + "s");
    }

    @Override
    public void onJogoTerminado(boolean vitoria) {
        if (vitoria) {
            statusLabel.setForeground(ACCENT_GOLD);
            statusLabel.setText("VITORIA! Missao cumprida!");
            log("═════════════════════════════");
            log("  PARABENS, AVENTUREIRO!");
            log("  Voce encontrou o Calice!");
            log("═════════════════════════════");
            JOptionPane.showMessageDialog(this,
                    "Voce venceu! Missao cumprida!",
                    "VITORIA", JOptionPane.INFORMATION_MESSAGE);
        } else {
            statusLabel.setForeground(new Color(0xFF4444));
            statusLabel.setText("Tempo esgotado - Fim de Jogo");
            log("═════════════════════════════");
            log("  TEMPO ESGOTADO");
            log("  A missao fracassou...");
            log("═════════════════════════════");
            JOptionPane.showMessageDialog(this,
                    "Tempo esgotado! Fim de jogo.",
                    "FIM DE JOGO", JOptionPane.WARNING_MESSAGE);
        }
    }
    @Override
    public void onMovimentoRealizado(int movRestantes) {
        Color cor = movRestantes <= 3 ? new Color(0xFF4444)
                : movRestantes <= 5 ? new Color(0xFFAA00)
                : ACCENT_PURPLE;
        movesLabel.setForeground(cor);
        movesLabel.setText("Mov: " + movRestantes);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────────────
    // ScrollBar escura customizada
    // ─────────────────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────────────
    // Entry point
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(GameGUI::new);
    }
}