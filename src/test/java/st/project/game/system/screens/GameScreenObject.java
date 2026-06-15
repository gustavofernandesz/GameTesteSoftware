package st.project.game.system.screens;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.fixture.FrameFixture;
import javax.swing.*;

/**
 * GameScreenObject — Screen Object da tela de jogo (GameGUI).
 * Agora os componentes possuem names definidos para alta testabilidade.
 */
public class GameScreenObject extends BaseScreen {

    public GameScreenObject(FrameFixture window) {
        super(window);
    }

    // ── Movimentos ────────────────────────────────────────────────────────

    public GameScreenObject moverNorte() {
        window.button("norte").click();
        return this;
    }

    public GameScreenObject moverSul() {
        window.button("sul").click();
        return this;
    }

    public GameScreenObject moverLeste() {
        window.button("leste").click();
        return this;
    }

    public GameScreenObject moverOeste() {
        window.button("oeste").click();
        return this;
    }

    // ── Leitura de labels por nome ───────────────────────────────────────

    public String textoScore() {
        return window.label("scoreLabel").text();
    }

    public String textoTempo() {
        return window.label("timeLabel").text();
    }

    public String textoMovimentos() {
        return window.label("movesLabel").text();
    }

    public String textoNivel() {
        return window.label("levelLabel").text();
    }

    public String textoAndar() {
        return window.label("andarLabel").text();
    }

    public String textoLog() {
        // Mantido com GenericTypeMatcher pois é o único JTextArea da tela
        // (mas você pode dar setName("logArea") nele no código real depois!)
        return window.textBox(new GenericTypeMatcher<JTextArea>(JTextArea.class) {
            @Override protected boolean isMatching(JTextArea t) { return true; }
        }).text();
    }

    // ── Verificações ──────────────────────────────────────────────────────

    public GameScreenObject verificarJanelaJogoAberta() {
        window.requireVisible();
        return this;
    }

    public GameScreenObject verificarLabelsVisiveis() {
        window.label("scoreLabel").requireVisible();
        window.label("timeLabel").requireVisible();
        window.label("movesLabel").requireVisible();
        window.label("levelLabel").requireVisible();
        window.label("andarLabel").requireVisible();
        return this;
    }

    public GameScreenObject verificarLogVisivel() {
        window.textBox(new GenericTypeMatcher<JTextArea>(JTextArea.class) {
            @Override protected boolean isMatching(JTextArea t) { return true; }
        }).requireVisible();
        return this;
    }
}