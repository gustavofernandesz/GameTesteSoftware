package st.project.game.system.screens;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.fixture.FrameFixture;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * GameScreenObject — Screen Object da tela de jogo (GameGUI).
 *
 * Labels reais (campos privados em GameGUI, sem setName()):
 *   scoreLabel  → texto inicial "Score: 0"
 *   timeLabel   → texto inicial "Tempo: 120s"
 *   movesLabel  → texto inicial "Mov: 20"
 *   levelLabel  → texto inicial "Nível: 1"
 *   andarLabel  → texto inicial "Andar: 1/4"
 *   logArea     → JTextArea (único editável=false na janela)
 *
 * Como os labels NÃO têm setName(), são localizados pelo texto que contêm.
 * O logArea é o único JTextArea na janela.
 *
 * Mapeamento de teclas real (GameGUI.buildKeyBindings()):
 *   VK_W / VK_UP    → norte
 *   VK_S / VK_DOWN  → sul
 *   VK_A / VK_LEFT  → oeste
 *   VK_D / VK_RIGHT → leste
 */
public class GameScreenObject extends BaseScreen {

    public GameScreenObject(FrameFixture window) {
        super(window);
    }

    // ── Movimentos ────────────────────────────────────────────────────────

    public GameScreenObject moverNorte() {
        window.pressAndReleaseKeys(KeyEvent.VK_W);
        return this;
    }

    public GameScreenObject moverSul() {
        window.pressAndReleaseKeys(KeyEvent.VK_S);
        return this;
    }

    public GameScreenObject moverLeste() {
        window.pressAndReleaseKeys(KeyEvent.VK_D);
        return this;
    }

    public GameScreenObject moverOeste() {
        window.pressAndReleaseKeys(KeyEvent.VK_A);
        return this;
    }

    // ── Leitura de labels por texto ───────────────────────────────────────

    /** Retorna o texto atual do label de score ("Score: N"). */
    public String textoScore() {
        return window.label(new GenericTypeMatcher<JLabel>(JLabel.class) {
            @Override protected boolean isMatching(JLabel l) {
                return l.getText() != null && l.getText().startsWith("Score:");
            }
        }).text();
    }

    /** Retorna o texto atual do label de tempo ("Tempo: Ns"). */
    public String textoTempo() {
        return window.label(new GenericTypeMatcher<JLabel>(JLabel.class) {
            @Override protected boolean isMatching(JLabel l) {
                return l.getText() != null && l.getText().startsWith("Tempo:");
            }
        }).text();
    }

    /** Retorna o texto atual do label de movimentos ("Mov: N"). */
    public String textoMovimentos() {
        return window.label(new GenericTypeMatcher<JLabel>(JLabel.class) {
            @Override protected boolean isMatching(JLabel l) {
                return l.getText() != null && l.getText().startsWith("Mov:");
            }
        }).text();
    }

    /** Retorna o texto atual do label de nível ("Nível: N"). */
    public String textoNivel() {
        return window.label(new GenericTypeMatcher<JLabel>(JLabel.class) {
            @Override protected boolean isMatching(JLabel l) {
                return l.getText() != null && l.getText().startsWith("Nível:");
            }
        }).text();
    }

    /** Retorna o texto atual do label de andar ("Andar: N/4"). */
    public String textoAndar() {
        return window.label(new GenericTypeMatcher<JLabel>(JLabel.class) {
            @Override protected boolean isMatching(JLabel l) {
                return l.getText() != null && l.getText().startsWith("Andar:");
            }
        }).text();
    }

    /** Retorna o conteúdo do log de ações (único JTextArea da janela). */
    public String textoLog() {
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
        // Verifica que todos os labels existem e estão visíveis
        textoScore();
        textoTempo();
        textoMovimentos();
        textoNivel();
        textoAndar();
        return this;
    }

    public GameScreenObject verificarLogVisivel() {
        window.textBox(new GenericTypeMatcher<JTextArea>(JTextArea.class) {
            @Override protected boolean isMatching(JTextArea t) { return true; }
        }).requireVisible();
        return this;
    }
}
