package st.project.game.system;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.timing.Pause;
import org.junit.jupiter.api.*;
import st.project.game.system.screens.*;
import st.project.game.view.LoginScreen;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.Frame;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.timing.Timeout.timeout;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


@DisplayName("Testes de Sistema – Jornadas reais do usuário")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class GameSystemTest {

    private FrameFixture loginWindow;
    private FrameFixture menuWindow;
    private FrameFixture gameWindow;

    @BeforeAll
    static void instalarRepaintManager() {
        FailOnThreadViolationRepaintManager.install();
    }

    @BeforeEach
    void abrirLoginScreen() {

        LoginScreen frame = GuiActionRunner.execute(
                (java.util.concurrent.Callable<LoginScreen>) LoginScreen::new);
        loginWindow = new FrameFixture(frame);
        loginWindow.show();
    }

    @AfterEach
    void fecharTodasAsJanelas() {
        if (gameWindow != null) {
            try { gameWindow.cleanUp(); } catch (Exception ignored) {}
            gameWindow = null;
        }

        if (menuWindow != null) {
            try { menuWindow.cleanUp(); } catch (Exception ignored) {}
            menuWindow = null;
        }

        if (loginWindow != null) {
            try { loginWindow.cleanUp(); } catch (Exception ignored) {}
            loginWindow = null;
        }

        for (Frame f : Frame.getFrames()) {
            if (f.isDisplayable()) {
                GuiActionRunner.execute((java.util.concurrent.Callable<Void>) () -> {
                    f.dispose();
                    return null;
                });
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // J1. Tela de login exibe todos os componentes
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J1: abrir sistema exibe campos de login, senha e botões esperados")
    void j1_abrirSistemaExibeTelaDeLogin() {
        new LoginScreenObject(loginWindow)
                .verificarCampoLoginVisivel()
                .verificarCampoSenhaVisivel()
                .verificarBotaoEntrarVisivel()
                .verificarBotaoCadastrarVisivel()
                .verificarBotaoRankingVisivel();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // J2. Login inválido → erro visível
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J2: login inválido exibe 'Login ou senha incorretos' e permanece na tela")
    void j2_loginInvalidoExibeMensagemDeErro() {
        LoginScreenObject login = new LoginScreenObject(loginWindow);

        login.preencherLogin("usuario_inexistente_" + UUID.randomUUID())
                .preencherSenha("senha_errada_xyz")
                .clicarEntrar();

        loginWindow.optionPane(timeout(3000))
                .requireMessage("Login ou senha incorretos.")
                .okButton().click();

        login.verificarBotaoEntrarVisivel();
    }

    @Test
    @DisplayName("J2b: campos vazios exibem 'Preencha todos os campos'")
    void j2b_loginCamposVaziosExibeMensagemDeErro() {
        new LoginScreenObject(loginWindow).clicarEntrar();

        loginWindow.optionPane(timeout(3000))
                .requireMessage("Preencha todos os campos.")
                .okButton().click();

        new LoginScreenObject(loginWindow).verificarBotaoEntrarVisivel();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // J3. Cadastro válido
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J3: cadastro com nome único exibe 'Conta criada com sucesso'")
    void j3_cadastroValidoCriaUsuario() {
        String novoUsuario = "usr_" + UUID.randomUUID().toString().substring(0, 8);

        new LoginScreenObject(loginWindow).clicarCadastrar();

        JOptionPaneFixture opcao = loginWindow.optionPane(timeout(3000));
        textBoxNoOptPane(opcao, 0).enterText(novoUsuario); // loginF
        textBoxNoOptPane(opcao, 1).enterText("Senha@123"); // passF
        opcao.okButton().click();

        loginWindow.optionPane(timeout(3000))
                .requireMessage("Conta criada com sucesso!")
                .okButton().click();

        new LoginScreenObject(loginWindow).verificarBotaoEntrarVisivel();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // J4a. Cadastro com nome vazio → erro
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J4a: cadastro com nome vazio exibe 'Login e senha são obrigatórios'")
    void j4a_cadastroNomeVazioExibeErro() {
        new LoginScreenObject(loginWindow).clicarCadastrar();

        JOptionPaneFixture opcao = loginWindow.optionPane(timeout(3000));
        textBoxNoOptPane(opcao, 1).enterText("SenhaQualquer"); // loginF vazio
        opcao.okButton().click();

        loginWindow.optionPane(timeout(3000))
                .requireMessage("Login e senha são obrigatórios.")
                .okButton().click();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // J4b. Cadastro duplicado → erro
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J4b: cadastrar o mesmo nome duas vezes exibe 'Login já existe'")
    void j4b_cadastroDuplicadoExibeErro() {
        String nome = "dup_" + UUID.randomUUID().toString().substring(0, 8);

        // Primeira vez: cria
        new LoginScreenObject(loginWindow).clicarCadastrar();
        JOptionPaneFixture opcao1 = loginWindow.optionPane(timeout(3000));
        textBoxNoOptPane(opcao1, 0).enterText(nome);
        textBoxNoOptPane(opcao1, 1).enterText("SenhaA");
        opcao1.okButton().click();
        loginWindow.optionPane(timeout(3000)).okButton().click();

        // Segunda vez: duplicata
        new LoginScreenObject(loginWindow).clicarCadastrar();
        JOptionPaneFixture opcao2 = loginWindow.optionPane(timeout(3000));
        textBoxNoOptPane(opcao2, 0).enterText(nome);
        textBoxNoOptPane(opcao2, 1).enterText("SenhaB");
        opcao2.okButton().click();

        loginWindow.optionPane(timeout(3000))
                .requireMessage("Login já existe. Escolha outro.")
                .okButton().click();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // J5. Login válido → MainMenu abre
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J5: login válido abre MainMenu com botões Novo Jogo, Ranking e Sair")
    void j5_loginValidoAbreMenuPrincipal() {
        criarELogar();

        new MainMenuScreenObject(menuWindow)
                .verificarMenuPrincipalAberto()
                .verificarBotaoNovoJogoVisivel()
                .verificarBotaoRankingVisivel()
                .verificarBotaoSairVisivel();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // J6. Novo jogo → GameGUI abre com labels e log
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J6: novo jogo abre GameGUI com score, tempo, movimentos, nível, andar e log")
    void j6_iniciarNovoJogoExibeGameGUI() {
        criarELogar();
        new MainMenuScreenObject(menuWindow).clicarNovoJogo();

        gameWindow = aguardarJanelaPorTitulo("Aventura Mágica", 4000);

        GameScreenObject game = new GameScreenObject(gameWindow);
        game.verificarJanelaJogoAberta()
                .verificarLabelsVisiveis()
                .verificarLogVisivel();

        assertThat(game.textoScore()).startsWith("Score:");
        assertThat(game.textoTempo()).startsWith("Tempo:");
        assertThat(game.textoMovimentos()).startsWith("Mov:");
        assertThat(game.textoNivel()).startsWith("Nível:");
        assertThat(game.textoAndar()).startsWith("Andar:");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // J7. Movimento válido → movimentos diminuem e log muda
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J7: pressionar D (leste) diminui movimentos restantes e popula log")
    void j7_movimentoValidoAlteraMovimentosELog() {
        criarELogar();
        new MainMenuScreenObject(menuWindow).clicarNovoJogo();
        gameWindow = aguardarJanelaPorTitulo("Aventura Mágica", 4000);

        GameScreenObject game = new GameScreenObject(gameWindow);
        String movAntes = game.textoMovimentos();
        String logAntes = game.textoLog();

        game.moverLeste();
        Pause.pause(500, TimeUnit.MILLISECONDS);

        assertThat(game.textoMovimentos())
                .as("Movimentos devem ter diminuído após movimento válido")
                .isNotEqualTo(movAntes);
        assertThat(game.textoLog())
                .as("Log deve ter sido populado após o movimento")
                .isNotEqualTo(logAntes);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // J8. Movimento bloqueado → log indica bloqueio, contador intacto
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J8: W (norte) na entrada loga 'Bloqueado' e não consome movimento")
    void j8_movimentoBloqueadoLogaMensagemEJogoContinua() {
        criarELogar();
        new MainMenuScreenObject(menuWindow).clicarNovoJogo();
        gameWindow = aguardarJanelaPorTitulo("Aventura Mágica", 4000);

        GameScreenObject game = new GameScreenObject(gameWindow);
        String movAntes = game.textoMovimentos();

        game.moverNorte();
        Pause.pause(500, TimeUnit.MILLISECONDS);

        assertThat(game.textoLog())
                .as("Log deve conter 'Bloqueado'")
                .containsIgnoringCase("Bloqueado");
        assertThat(game.textoMovimentos())
                .as("Movimento inválido não deve consumir o contador")
                .isEqualTo(movAntes);

        game.moverLeste();
        Pause.pause(400, TimeUnit.MILLISECONDS);
        assertThat(game.textoMovimentos())
                .as("Movimento válido após bloqueio deve ser aceito")
                .isNotEqualTo(movAntes);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // J9. Ranking → diálogo abre e fecha
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J9: Ver Ranking exibe RankingDialog e fecha corretamente")
    void j9_rankingPelaTelaDeLoginAbreEFecha() {
        new LoginScreenObject(loginWindow).clicarRanking();

        DialogFixture ranking = loginWindow.dialog(timeout(3000));

        new RankingScreenObject(loginWindow, ranking)
                .verificarRankingAberto()
                .verificarConteudoVisivel()
                .fechar();

        new LoginScreenObject(loginWindow).verificarBotaoEntrarVisivel();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // J10. Sair → volta ao login
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J10: Sair no MainMenu fecha o menu e abre nova tela de login")
    void j10_sairDoMenuPrincipalExecutaSemExcecao() {
        criarELogar();

        // Snapshot dos frames existentes ANTES de clicar Sair.
        // aguardarNovaJanelaComTitulo(Set) exclui esses frames da busca,
        // evitando encontrar o loginWindow antigo (já disposed por criarELogar).
        Set<Frame> framesAntes = new HashSet<>(Arrays.asList(Frame.getFrames()));

        new MainMenuScreenObject(menuWindow).clicarSair();

        // O botão Sair chama dispose() no MainMenu e new LoginScreen().
        // Buscamos apenas janelas que NÃO existiam antes do clique.
        FrameFixture novoLogin = aguardarNovaJanelaComTitulo(
                "login", framesAntes, 8_000);

        try {
            new LoginScreenObject(novoLogin).verificarBotaoEntrarVisivel();
        } finally {
            novoLogin.cleanUp();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Utilitários privados
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Cria usuário via GUI e faz login. Preenche {@code menuWindow}.
     *
     * CORREÇÃO DO TRAVAMENTO:
     * LoginScreen.login() chama this.dispose() antes de abrir o MainMenu.
     * Depois de clicarEntrar(), o loginWindow aponta para uma janela destruída.
     * Se o robot tentar interagir com ela, trava indefinidamente.
     *
     * Solução:
     * 1. Após cadastrar, desacoplamos o robot do loginWindow com cleanUp().
     * 2. Chamamos clicarEntrar() diretamente no componente antes do cleanUp.
     * 3. Buscamos o MainMenu pelo título assim que ele aparecer.
     */
    private String criarELogar() {
        String usuario = "sys_" + UUID.randomUUID().toString().substring(0, 8);
        String senha   = "Pass1234";

        new LoginScreenObject(loginWindow).clicarCadastrar();

        JOptionPaneFixture opcao = loginWindow.optionPane(timeout(3000));
        textBoxNoOptPane(opcao, 0).enterText(usuario);
        textBoxNoOptPane(opcao, 1).enterText(senha);
        opcao.okButton().click();

        loginWindow.optionPane(timeout(3000)).okButton().click();

        new LoginScreenObject(loginWindow)
                .preencherLogin(usuario)
                .preencherSenha(senha)
                .clicarEntrar();

        menuWindow = aguardarJanelaPorTitulo("Menu Principal - " + usuario, 8000);

        loginWindow = null;

        return usuario;
    }

    /**
     * Localiza campos de texto dentro de um JOptionPane por índice de ordem.
     * textBox(int) não existe na API do AssertJ Swing — usamos GenericTypeMatcher.
     *
     * Ordem em LoginScreen.createAccount():
     *   0 → loginF  (JTextField)
     *   1 → passF   (JPasswordField)
     *   2 → avatarF (JTextField, padrão "avatar1.png")
     */
    private JTextComponentFixture textBoxNoOptPane(JOptionPaneFixture pane, int indice) {
        final int[] contador = {0};
        return pane.textBox(new GenericTypeMatcher<JTextComponent>(JTextComponent.class) {
            @Override
            protected boolean isMatching(JTextComponent c) {
                if (!c.isVisible()) return false;
                if (contador[0] == indice) {
                    contador[0]++;
                    return true;
                }
                contador[0]++;
                return false;
            }
        });
    }

    /**
     * Aguarda JFrame cujo título contenha {@code fragmento} (case-insensitive).
     * Usa título em vez de instanceof para ser agnóstico ao tipo concreto.
     */
    private FrameFixture aguardarJanelaPorTitulo(String fragmento, long timeoutMs) {
        long fim = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < fim) {
            for (Frame f : Frame.getFrames()) {
                if (f.isVisible()
                        && f instanceof JFrame
                        && f.getTitle() != null
                        && f.getTitle().toLowerCase().contains(fragmento.toLowerCase())) {
                    return new FrameFixture((JFrame) f);
                }
            }
            Pause.pause(100, TimeUnit.MILLISECONDS);
        }
        throw new AssertionError(
                "Nenhuma janela com título contendo '" + fragmento
                        + "' ficou visível em " + timeoutMs + "ms");
    }

    /**
     * Igual a aguardarJanelaPorTitulo, mas ignora {@code excluir}.
     *
     * Necessário no J10: Frame.getFrames() ainda lista o loginWindow antigo
     * (destruído por LoginScreen.dispose() dentro de criarELogar()) porque o
     * GC ainda não o coletou. Sem a exclusão, o helper encontrava a janela
     * antiga e nunca chegava ao novo LoginScreen aberto pelo botão Sair.
     */
    private FrameFixture aguardarNovaJanelaComTitulo(String fragmento, Frame excluir, long timeoutMs) {
        long fim = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < fim) {
            for (Frame f : Frame.getFrames()) {
                if (f != excluir
                        && f.isVisible()
                        && f instanceof JFrame
                        && f.getTitle() != null
                        && f.getTitle().toLowerCase().contains(fragmento.toLowerCase())) {
                    return new FrameFixture((JFrame) f);
                }
            }
            Pause.pause(100, TimeUnit.MILLISECONDS);
        }
        throw new AssertionError(
                "Nenhuma janela nova com título contendo '" + fragmento
                        + "' ficou visível em " + timeoutMs + "ms");
    }


    private void aguardarFrameSumir(JFrame frame, long timeoutMs) {
        long fim = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < fim) {
            if (!frame.isShowing() || !frame.isDisplayable()) {
                return;
            }

            Pause.pause(100, TimeUnit.MILLISECONDS);
        }

        throw new AssertionError(
                "A janela '" + frame.getTitle() + "' não fechou em " + timeoutMs + "ms"
        );
    }
    private FrameFixture aguardarNovaJanelaComTitulo(
            String fragmento,
            Set<Frame> framesAntigos,
            long timeoutMs
    ) {
        long fim = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < fim) {
            for (Frame f : Frame.getFrames()) {
                if (!framesAntigos.contains(f)
                        && f.isVisible()
                        && f instanceof JFrame
                        && f.getTitle() != null
                        && f.getTitle().toLowerCase().contains(fragmento.toLowerCase())) {

                    JFrame frame = (JFrame) f;

                    GuiActionRunner.execute((java.util.concurrent.Callable<Void>) () -> {
                        frame.toFront();
                        frame.requestFocus();
                        return null;
                    });

                    return new FrameFixture(frame);
                }
            }

            Pause.pause(100, TimeUnit.MILLISECONDS);
        }

        throw new AssertionError(
                "Nenhuma janela NOVA com título contendo '" + fragmento
                        + "' ficou visível em " + timeoutMs + "ms"
        );
    }


}