package st.project.game.system.screens;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTextComponentFixture;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

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
        // 1. Em vez de simular o clique físico do mouse que bloqueia a execução,
        // pegamos a referência ao componente JButton real da tela.
        javax.swing.JButton botao = botaoPorTexto("Entrar").target();

        // 2. Disparamos o método doClick() programático de forma assíncrona na EDT.
        // Isso faz com que o popup abra, mas o robô NÃO fique esperando ele fechar.
        javax.swing.SwingUtilities.invokeLater(botao::doClick);

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
    // No LoginScreenObject.java:
    public MainMenuScreenObject clicarEntrarComSucesso(java.util.Set<java.awt.Frame> framesAntigos) {
        // 1. Localiza o botão e dispara o clique de forma assíncrona para evitar travar a EDT
        javax.swing.JButton botao = botaoPorTexto("Entrar").target();
        javax.swing.SwingUtilities.invokeLater(botao::doClick);

        // 2. Usa o método herdado da BaseScreen para capturar a nova janela que vai se abrir
        org.assertj.swing.fixture.FrameFixture menuWin = aguardarNovaJanelaComTitulo("Menu Principal", framesAntigos, 8000);

        // 3. Retorna o próximo Screen Object encadeando a jornada de forma fluente!
        return new MainMenuScreenObject(menuWin);
    }
}