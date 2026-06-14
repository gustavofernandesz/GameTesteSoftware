package st.project.game.system.screens;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.timing.Pause;

import javax.swing.*;
import java.awt.*;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * BaseScreen — raiz da hierarquia de Screen Objects.
 *
 * Mantém a referência ao FrameFixture ativo para que todos os
 * Screen Objects filhos possam interagir com a janela sem precisar
 * receber o fixture repetidamente em cada método.
 *
 * Padrão aplicado: Screen Object (adaptação de Page Object para Swing).
 * Cada subclasse representa uma tela do jogo e expõe apenas as ações
 * e verificações visíveis ao usuário — nenhuma lógica de negócio é
 * acessada diretamente.
 */
public abstract class BaseScreen {

    /** Fixture da janela principal ativa no momento. */
    protected final FrameFixture window;

    protected BaseScreen(FrameFixture window) {
        this.window = window;
    }

    /** Retorna o robot subjacente para ações avançadas (teclas, esperas). */
    protected Robot robot() {
        return window.robot();
    }

    protected JButtonFixture botaoPorTexto(String texto) {
        return window.button(new GenericTypeMatcher<JButton>(JButton.class) {
            @Override protected boolean isMatching(JButton b) {
                return texto.equals(b.getText()) && b.isShowing();
            }
        });
    }

    /**
     * Aguarda e captura uma NOVA janela que apareceu no sistema operacional,
     * ignorando um conjunto de frames antigos informados.
     */
    public FrameFixture aguardarNovaJanelaComTitulo(
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

                    // Utiliza o robô compartilhado da janela atual para manter a consistência do teste
                    return new FrameFixture(robot(), frame);
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
