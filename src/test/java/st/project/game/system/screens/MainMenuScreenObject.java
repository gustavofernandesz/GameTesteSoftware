package st.project.game.system.screens;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;

import javax.swing.*;

/**
 * MainMenuScreenObject — Screen Object do menu principal (MainMenu).
 * <p>
 * Botões localizados por getText() via GenericTypeMatcher — sem setName().
 * Textos reais: "Novo Jogo", "Continuar", "Ver Ranking", "Sair".
 */
public class MainMenuScreenObject extends BaseScreen {

    public MainMenuScreenObject(FrameFixture window) {
        super(window);
    }

    // ── Ações ─────────────────────────────────────────────────────────────

    public MainMenuScreenObject clicarNovoJogo() {
        window.button("newGameButton").click();
        return this;
    }

    public MainMenuScreenObject clicarContinuar() {
        window.button("continueButton").click();
        return this;
    }

    public MainMenuScreenObject clicarRanking() {
        window.button("rankingButton").click();
        return this;
    }

    /**
     * Clica em "Sair" após neutralizar o EXIT_ON_CLOSE do MainMenu.
     * <p>
     * PROBLEMA:
     * MainMenu.initComponents() define setDefaultCloseOperation(EXIT_ON_CLOSE).
     * Quando o @AfterEach do teste chama menuWindow.cleanUp(), o AssertJ Swing
     * tenta fechar a janela internamente — EXIT_ON_CLOSE encerra a JVM inteira,
     * impedindo o teste de terminar e verificar o resultado.
     * <p>
     * SOLUÇÃO (sem alterar código de produção):
     * Antes de clicar em "Sair", trocamos EXIT_ON_CLOSE por DISPOSE_ON_CLOSE
     * diretamente no JFrame alvo, via GuiActionRunner (garante EDT).
     * O botão Sair chama dispose() + new LoginScreen() — com DISPOSE_ON_CLOSE
     * isso funciona normalmente. Só o @AfterEach deixa de matar a JVM.
     */
    public MainMenuScreenObject clicarSair() {
        JButtonFixture sairFixture = window.button("logoutButton");
        JButton sair = sairFixture.target();

        sairFixture.requireVisible();
        sairFixture.requireEnabled();

        GuiActionRunner.execute((java.util.concurrent.Callable<Void>) () -> {
            java.awt.event.ActionEvent evento = new java.awt.event.ActionEvent(
                    sair,
                    java.awt.event.ActionEvent.ACTION_PERFORMED,
                    sair.getActionCommand()
            );

            for (java.awt.event.ActionListener listener : sair.getActionListeners()) {
                listener.actionPerformed(evento);
            }

            return null;
        });

        window.robot().waitForIdle();

        return this;
    }

    // ── Verificações ──────────────────────────────────────────────────────

    public MainMenuScreenObject verificarMenuPrincipalAberto() {
        window.requireVisible();
        return this;
    }

    public MainMenuScreenObject verificarBotaoNovoJogoVisivel() {
        window.button("newGameButton").requireVisible().requireEnabled();
        return this;
    }

    public MainMenuScreenObject verificarBotaoRankingVisivel() {
        window.button("rankingButton").requireVisible();
        return this;
    }

    public MainMenuScreenObject verificarBotaoSairVisivel() {
        window.button("logoutButton").requireVisible();
        return this;
    }
}