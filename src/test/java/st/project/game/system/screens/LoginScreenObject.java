package st.project.game.system.screens;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;

import javax.swing.*;

/**
 * LoginScreenObject — Screen Object da tela de login.
 *
 * IMPORTANTE: nenhum botão tem setName() no código de produção (name=null).
 * window.button("texto") no AssertJ Swing busca por NAME, não por texto.
 * Por isso usamos GenericTypeMatcher que compara getText() para localizar
 * botões, e tipo para localizar campos de texto.
 */
public class LoginScreenObject extends BaseScreen {

    public LoginScreenObject(FrameFixture window) {
        super(window);
    }

    // ── helpers de localização ────────────────────────────────────────────

    private JButtonFixture botaoPorTexto(String texto) {
        return window.button(new GenericTypeMatcher<JButton>(JButton.class) {
            @Override protected boolean isMatching(JButton b) {
                return texto.equals(b.getText()) && b.isShowing();
            }
        });
    }

    private JTextComponentFixture campoLogin() {
        return window.textBox(new GenericTypeMatcher<JTextField>(JTextField.class) {
            @Override protected boolean isMatching(JTextField f) {
                return !(f instanceof JPasswordField) && f.isShowing();
            }
        });
    }

    private JTextComponentFixture campoSenha() {
        return window.textBox(new GenericTypeMatcher<JPasswordField>(JPasswordField.class) {
            @Override protected boolean isMatching(JPasswordField f) {
                return f.isShowing();
            }
        });
    }

    // ── Ações ─────────────────────────────────────────────────────────────

    public LoginScreenObject preencherLogin(String login) {
        campoLogin().deleteText().enterText(login);
        return this;
    }

    public LoginScreenObject preencherSenha(String senha) {
        campoSenha().deleteText().enterText(senha);
        return this;
    }

    public LoginScreenObject clicarEntrar() {
        botaoPorTexto("Entrar").click();
        return this;
    }

    public LoginScreenObject clicarCadastrar() {
        window.focus();
        botaoPorTexto("Criar Conta").requireVisible().requireEnabled().click();
        return this;
    }

    public LoginScreenObject clicarRanking() {
        botaoPorTexto("Ver Ranking").click();
        return this;
    }

    // ── Verificações ──────────────────────────────────────────────────────

    public LoginScreenObject verificarCampoLoginVisivel() {
        campoLogin().requireVisible().requireEnabled();
        return this;
    }

    public LoginScreenObject verificarCampoSenhaVisivel() {
        campoSenha().requireVisible().requireEnabled();
        return this;
    }

    public LoginScreenObject verificarBotaoEntrarVisivel() {
        botaoPorTexto("Entrar").requireVisible().requireEnabled();
        return this;
    }

    public LoginScreenObject verificarBotaoCadastrarVisivel() {
        botaoPorTexto("Criar Conta").requireVisible();
        return this;
    }

    public LoginScreenObject verificarBotaoRankingVisivel() {
        botaoPorTexto("Ver Ranking").requireVisible();
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
}