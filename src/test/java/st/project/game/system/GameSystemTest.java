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

    private org.assertj.swing.core.Robot robot;
    private FrameFixture loginWindow;
    private FrameFixture menuWindow;
    private FrameFixture gameWindow;

    @BeforeAll
    static void instalarRepaintManager() {
        FailOnThreadViolationRepaintManager.install();
    }

    @BeforeEach
    void abrirLoginScreen() {
        robot = org.assertj.swing.core.BasicRobot.robotWithNewAwtHierarchy();
        LoginScreen frame = GuiActionRunner.execute(
                (java.util.concurrent.Callable<LoginScreen>) LoginScreen::new);
        loginWindow = new FrameFixture(robot, frame);
        loginWindow.show();
    }

    @AfterEach
    void fecharTodasAsJanelas() {
        if (robot != null) {
            try {
                robot.cleanUp();
            } catch (Exception ignored) {}
            robot = null;
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
        textBoxNoOptPane(opcao, 0).enterText(novoUsuario);
        textBoxNoOptPane(opcao, 1).enterText("Senha@123");
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
        textBoxNoOptPane(opcao, 1).enterText("SenhaQualquer");
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

        new LoginScreenObject(loginWindow).clicarCadastrar();
        JOptionPaneFixture opcao1 = loginWindow.optionPane(timeout(3000));
        textBoxNoOptPane(opcao1, 0).enterText(nome);
        textBoxNoOptPane(opcao1, 1).enterText("SenhaA");
        opcao1.okButton().click();
        loginWindow.optionPane(timeout(3000)).okButton().click();

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
    // J5. Login válido → MainMenu abre (FLUÊNCIA CONQUISTADA AQUI!)
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J5: login válido abre MainMenu com botões Novo Jogo, Ranking e Sair")
    void j5_loginValidoAbreMenuPrincipal() {
        // 1. Preparamos o cadastro de um usuário de teste
        String usuario = "sys_" + UUID.randomUUID().toString().substring(0, 8);
        cadastrarUsuarioAuxiliar(usuario, "Pass1234");

        // Mapeia o snapshot das janelas antes do clique definitivo de login
        Set<Frame> antes = new HashSet<>(Arrays.asList(Frame.getFrames()));

        // 2. Executa a jornada de forma 100% contínua e elegante!
        LoginScreenObject login = new LoginScreenObject(loginWindow);

        MainMenuScreenObject menu = login
                .preencherLogin(usuario)
                .preencherSenha("Pass1234")
                .clicarEntrarComSucesso(antes); // Método fluente mapeado!

        menu.verificarMenuPrincipalAberto()
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
        String usuario = "sys_" + UUID.randomUUID().toString().substring(0, 8);
        cadastrarUsuarioAuxiliar(usuario, "Pass1234");
        Set<Frame> antes = new HashSet<>(Arrays.asList(Frame.getFrames()));

        MainMenuScreenObject menu = new LoginScreenObject(loginWindow)
                .preencherLogin(usuario)
                .preencherSenha("Pass1234")
                .clicarEntrarComSucesso(antes);

        menu.clicarNovoJogo();
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
        String usuario = "sys_" + UUID.randomUUID().toString().substring(0, 8);
        cadastrarUsuarioAuxiliar(usuario, "Pass1234");
        Set<Frame> antes = new HashSet<>(Arrays.asList(Frame.getFrames()));

        new LoginScreenObject(loginWindow)
                .preencherLogin(usuario)
                .preencherSenha("Pass1234")
                .clicarEntrarComSucesso(antes)
                .clicarNovoJogo();

        gameWindow = aguardarJanelaPorTitulo("Aventura Mágica", 4000);

        GameScreenObject game = new GameScreenObject(gameWindow);
        String movAntes = game.textoMovimentos();
        String logAntes = game.textoLog();

        game.moverLeste();
        Pause.pause(500, TimeUnit.MILLISECONDS);

        assertThat(game.textoMovimentos()).isNotEqualTo(movAntes);
        assertThat(game.textoLog()).isNotEqualTo(logAntes);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // J8. Movimento bloqueado → log indica bloqueio, contador intacto
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J8: W (norte) na entrada loga 'Bloqueado' e não consome movimento")
    void j8_movimentoBloqueadoLogaMensagemEJogoContinua() {
        String usuario = "sys_" + UUID.randomUUID().toString().substring(0, 8);
        cadastrarUsuarioAuxiliar(usuario, "Pass1234");
        Set<Frame> antes = new HashSet<>(Arrays.asList(Frame.getFrames()));

        new LoginScreenObject(loginWindow)
                .preencherLogin(usuario)
                .preencherSenha("Pass1234")
                .clicarEntrarComSucesso(antes)
                .clicarNovoJogo();

        gameWindow = aguardarJanelaPorTitulo("Aventura Mágica", 4000);

        GameScreenObject game = new GameScreenObject(gameWindow);
        String movAntes = game.textoMovimentos();

        game.moverNorte();
        Pause.pause(500, TimeUnit.MILLISECONDS);

        assertThat(game.textoLog()).containsIgnoringCase("Bloqueado");
        assertThat(game.textoMovimentos()).isEqualTo(movAntes);

        game.moverLeste();
        Pause.pause(400, TimeUnit.MILLISECONDS);
        assertThat(game.textoMovimentos()).isNotEqualTo(movAntes);
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
        String usuario = "sys_" + UUID.randomUUID().toString().substring(0, 8);
        cadastrarUsuarioAuxiliar(usuario, "Pass1234");
        Set<Frame> antesDoLogin = new HashSet<>(Arrays.asList(Frame.getFrames()));

        MainMenuScreenObject menu = new LoginScreenObject(loginWindow)
                .preencherLogin(usuario)
                .preencherSenha("Pass1234")
                .clicarEntrarComSucesso(antesDoLogin);

        Set<Frame> framesAntesDeSair = new HashSet<>(Arrays.asList(Frame.getFrames()));
        menu.clicarSair();

        LoginScreenObject wrapperHelper = new LoginScreenObject(loginWindow);
        FrameFixture novoLogin = wrapperHelper.aguardarNovaJanelaComTitulo("login", framesAntesDeSair, 8000);

        try {
            new LoginScreenObject(novoLogin).verificarBotaoEntrarVisivel();
        } finally {
            novoLogin.cleanUp();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Utilitários privados do teste
    // ═══════════════════════════════════════════════════════════════════════

    /** Apenas realiza o fluxo do popup de cadastro para dar suporte às jornadas */
    private void cadastrarUsuarioAuxiliar(String usuario, String senha) {
        new LoginScreenObject(loginWindow).clicarCadastrar();
        JOptionPaneFixture opcao = loginWindow.optionPane(timeout(3000));
        textBoxNoOptPane(opcao, 0).enterText(usuario);
        textBoxNoOptPane(opcao, 1).enterText(senha);
        opcao.okButton().click();
        loginWindow.optionPane(timeout(3000)).okButton().click();
    }

    private JTextComponentFixture textBoxNoOptPane(JOptionPaneFixture pane, int indice) {
        final int[] contador = {0};
        return pane.textBox(new GenericTypeMatcher<>(JTextComponent.class) {
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

    private FrameFixture aguardarJanelaPorTitulo(String fragmento, long timeoutMs) {
        long fim = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < fim) {
            for (Frame f : Frame.getFrames()) {
                if (f.isVisible()
                        && f instanceof JFrame
                        && f.getTitle() != null
                        && f.getTitle().toLowerCase().contains(fragmento.toLowerCase())) {

                    return new FrameFixture(robot, f);
                }
            }
            Pause.pause(100, TimeUnit.MILLISECONDS);
        }
        throw new AssertionError(
                "Nenhuma janela com título contendo '" + fragmento
                        + "' ficou visível em " + timeoutMs + "ms");
    }
}