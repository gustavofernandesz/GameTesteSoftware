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
    void limparArquivosE_AbrirLoginScreen() {
        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get("usuarios.txt"));
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get("ranking.txt"));
            // Limpa a pasta de saves, etc...
        } catch (java.io.IOException e) {
            // ignora
        }

        robot = org.assertj.swing.core.BasicRobot.robotWithNewAwtHierarchy();

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
    // J11. Continuar Jogo → Restaura o estado salvo
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J11: Continuar jogo deve restaurar a exata quantidade de movimentos e estado anterior")
    void j11_continuarJogoRestauraEstadoAnterior() {
        // 1. Setup: Criamos o usuário e logamos
        String usuario = "sys_" + UUID.randomUUID().toString().substring(0, 8);
        cadastrarUsuarioAuxiliar(usuario, "Pass1234");
        Set<Frame> antesMenu = new HashSet<>(Arrays.asList(Frame.getFrames()));

        MainMenuScreenObject menu = new LoginScreenObject(loginWindow)
                .preencherLogin(usuario)
                .preencherSenha("Pass1234")
                .clicarEntrarComSucesso(antesMenu);

        // 2. Iniciar um Novo Jogo
        menu.clicarNovoJogo();
        gameWindow = aguardarJanelaPorTitulo("Aventura Mágica", 4000);
        GameScreenObject game = new GameScreenObject(gameWindow);

        // 3. Modificar o estado do jogo (fazer um movimento válido)
        String movAntes = game.textoMovimentos();
        game.moverLeste();

        // Espera condicional melhorada para evitar flakiness (espera o label atualizar)
        long startTime = System.currentTimeMillis();
        while (game.textoMovimentos().equals(movAntes) && (System.currentTimeMillis() - startTime) < 2000) {
            Pause.pause(100, TimeUnit.MILLISECONDS);
        }

        String movimentosAposAndar = game.textoMovimentos();
        assertThat(movimentosAposAndar)
                .as("O movimento deveria ter alterado o texto de movimentos")
                .isNotEqualTo(movAntes);

        // 4. Fechar o jogo / Voltar ao menu principal
        // ATENÇÃO: Dependendo de como seu jogo funciona, você pode ter um botão 'Voltar' ou precisar fechar o frame.
        // Aqui simulo fechando a janela do jogo e voltando para a tela de login para relogar e Continuar.
        gameWindow.cleanUp(); // Fecha a janela do jogo

        // Vamos relogar para simular um novo acesso (ou você pode clicar em um botão Voltar se houver)
        abrirLoginScreen(); // Reabre a tela de login
        Set<Frame> antesNovoMenu = new HashSet<>(Arrays.asList(Frame.getFrames()));

        MainMenuScreenObject novoMenu = new LoginScreenObject(loginWindow)
                .preencherLogin(usuario)
                .preencherSenha("Pass1234")
                .clicarEntrarComSucesso(antesNovoMenu);

        // 5. Clicar em "Continuar"
        novoMenu.clicarContinuar();
        FrameFixture novoGameWindow = aguardarJanelaPorTitulo("Aventura Mágica", 4000);
        GameScreenObject jogoContinuado = new GameScreenObject(novoGameWindow);

        // 6. Assertiva de Sistema: Verifica se o estado se manteve
        String movimentosAoContinuar = jogoContinuado.textoMovimentos();
        assertThat(movimentosAoContinuar)
                .as("A quantidade de movimentos ao continuar o jogo deve ser a mesma de quando o jogador saiu")
                .isEqualTo(movimentosAposAndar);

        // Limpeza final para não atrapalhar outros testes
        novoGameWindow.cleanUp();
    }
    // ═══════════════════════════════════════════════════════════════════════
    // J12. Atualização do Ranking pós-partida
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J12: Partida finalizada deve salvar a pontuação e exibir o usuário no Ranking")
    void j12_partidaFinalizadaAtualizaTabelaDeRanking() {
        // 1. Setup: Criamos um usuário com prefixo "rk_" para facilitar a identificação
        String usuarioRanking = "rk_" + UUID.randomUUID().toString().substring(0, 5);
        cadastrarUsuarioAuxiliar(usuarioRanking, "Pass1234");
        Set<Frame> antesMenu = new HashSet<>(Arrays.asList(Frame.getFrames()));

        MainMenuScreenObject menu = new LoginScreenObject(loginWindow)
                .preencherLogin(usuarioRanking)
                .preencherSenha("Pass1234")
                .clicarEntrarComSucesso(antesMenu);

        // 2. Iniciar um Novo Jogo
        menu.clicarNovoJogo();
        gameWindow = aguardarJanelaPorTitulo("Aventura Mágica", 4000);
        GameScreenObject game = new GameScreenObject(gameWindow);

        // 3. Jogar até o fim para gerar uma pontuação
        String textoMov = game.textoMovimentos();
        int movimentosRestantes = Integer.parseInt(textoMov.replaceAll("\\D+", ""));

        for (int i = 0; i < movimentosRestantes; i++) {
            if (i % 2 == 0) {
                game.moverLeste();
            } else {
                game.moverOeste();
            }
            Pause.pause(40, TimeUnit.MILLISECONDS);
        }

        // 4. Fechar o Pop-up de Fim de Jogo
        JOptionPaneFixture popupFim = gameWindow.optionPane(timeout(3000));
        popupFim.okButton().click();

        // 5. De volta ao Menu, abrir o Ranking
        FrameFixture menuReaberto = aguardarJanelaPorTitulo("Menu Principal", 4000);
        MainMenuScreenObject mainMenuFinal = new MainMenuScreenObject(menuReaberto);

        mainMenuFinal.clicarRanking();

        // 6. Assertiva de Sistema: Validar presença na Tabela
        // Capturamos o diálogo de ranking que acabou de abrir
        DialogFixture rankingDialog = menuReaberto.dialog(timeout(3000));
        RankingScreenObject ranking = new RankingScreenObject(menuReaberto, rankingDialog);

        ranking.verificarRankingAberto()
                .verificarUsuarioNaTabela(usuarioRanking) // A Mágica acontece aqui!
                .fechar();
    }


    // ═══════════════════════════════════════════════════════════════════════
    // J13. Fim de Jogo (Derrota) → Retorna ao Menu
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("J13: Condição de derrota fecha a partida e retorna ao Menu Principal")
    void j13_fimDeJogoRetornaAoMenuPrincipal() {
        // 1. Setup: Criamos o usuário e logamos
        String usuario = "sys_" + UUID.randomUUID().toString().substring(0, 8);
        cadastrarUsuarioAuxiliar(usuario, "Pass1234");
        Set<Frame> antesMenu = new HashSet<>(Arrays.asList(Frame.getFrames()));

        MainMenuScreenObject menu = new LoginScreenObject(loginWindow)
                .preencherLogin(usuario)
                .preencherSenha("Pass1234")
                .clicarEntrarComSucesso(antesMenu);

        // 2. Iniciar um Novo Jogo
        menu.clicarNovoJogo();
        gameWindow = aguardarJanelaPorTitulo("Aventura Mágica", 4000);
        GameScreenObject game = new GameScreenObject(gameWindow);

        // 3. Obter a quantidade total de movimentos (ex: "Mov: 20" -> extrai o 20)
        String textoMov = game.textoMovimentos();
        int movimentosRestantes = Integer.parseInt(textoMov.replaceAll("\\D+", ""));

        // 4. Ação: Gastar todos os movimentos simulando a derrota (anda Leste e Oeste repetidamente)
        for (int i = 0; i < movimentosRestantes; i++) {
            if (i % 2 == 0) {
                game.moverLeste();
            } else {
                game.moverOeste();
            }
            // Pausa minúscula para não atropelar a Event Dispatch Thread (EDT) do Swing
            Pause.pause(40, TimeUnit.MILLISECONDS);
        }

        // 5. Capturar o Pop-up de Fim de Jogo
        // Usamos timeout(3000) para aguardar o pop-up aparecer assim que o último movimento for feito
        JOptionPaneFixture popupFim = gameWindow.optionPane(timeout(3000));

        // Verifica o título que você me confirmou
        popupFim.requireTitle("FIM DE JOGO");

        // DICA: Se a mensagem de erro por falta de movimentos for diferente de "Tempo esgotado...",
        // descomente e altere a linha abaixo para validar a mensagem exata:
        // popupFim.requireMessage("Sua mensagem de fim de jogo por falta de movimentos aqui");

        // 6. Ação: Jogador clica no botão OK do pop-up
        popupFim.okButton().click();

        // 7. Assertiva de Sistema: O jogo deve fechar e o Menu Principal reaparecer
        // Como o frame de Jogo deve ter sofrido "dispose()", procuramos pela janela do Menu novamente
        FrameFixture menuReaberto = aguardarJanelaPorTitulo("Menu Principal", 4000);

        MainMenuScreenObject mainMenuFinal = new MainMenuScreenObject(menuReaberto);

        // Verifica se a tela do menu está totalmente interativa novamente
        mainMenuFinal.verificarMenuPrincipalAberto()
                .verificarBotaoNovoJogoVisivel();
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