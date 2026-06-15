package st.project.game.system.screens;

import org.assertj.swing.fixture.FrameFixture;
import java.awt.Frame;
import java.util.Set;

/**
 * LoginScreenObject — Screen Object da tela de login.
 * Atualizado para utilizar names, eliminando a dependência de textos visuais.
 */
public class LoginScreenObject extends BaseScreen {

    public LoginScreenObject(FrameFixture window) {
        super(window);
    }

    // ── Ações ─────────────────────────────────────────────────────────────

    public LoginScreenObject preencherLogin(String login) {
        window.textBox("loginField").deleteText().enterText(login);
        return this;
    }

    public LoginScreenObject preencherSenha(String senha) {
        window.textBox("passwordField").deleteText().enterText(senha);
        return this;
    }

    public LoginScreenObject clicarEntrar() {
        window.button("loginButton").click();
        return this;
    }

    public LoginScreenObject clicarCadastrar() {
        window.focus();
        window.button("createAccountButton").requireVisible().requireEnabled().click();
        return this;
    }

    public LoginScreenObject clicarRanking() {
        window.button("rankingButton").click();
        return this;
    }

    // ── Verificações ──────────────────────────────────────────────────────

    public LoginScreenObject verificarCampoLoginVisivel() {
        window.textBox("loginField").requireVisible().requireEnabled();
        return this;
    }

    public LoginScreenObject verificarCampoSenhaVisivel() {
        window.textBox("passwordField").requireVisible().requireEnabled();
        return this;
    }

    public LoginScreenObject verificarBotaoEntrarVisivel() {
        window.button("loginButton").requireVisible().requireEnabled();
        return this;
    }

    public LoginScreenObject verificarBotaoCadastrarVisivel() {
        window.button("createAccountButton").requireVisible();
        return this;
    }

    public LoginScreenObject verificarBotaoRankingVisivel() {
        window.button("rankingButton").requireVisible();
        return this;
    }

    public LoginScreenObject verificarMensagemDeErroVisivel() {
        window.optionPane().requireVisible();
        return this;
    }

    public LoginScreenObject fecharDialogoDeErro() {
        window.optionPane().okButton().click();
        return this;
    }

    // ── Transição de Jornada ──────────────────────────────────────────────

    public MainMenuScreenObject clicarEntrarComSucesso(Set<Frame> framesAntigos) {
        // Agora busca o botão diretamente pelo NAME
        javax.swing.JButton botao = window.button("loginButton").target();
        javax.swing.SwingUtilities.invokeLater(botao::doClick);

        org.assertj.swing.fixture.FrameFixture menuWin = aguardarNovaJanelaComTitulo("Menu Principal", framesAntigos, 8000);

        return new MainMenuScreenObject(menuWin);
    }
}