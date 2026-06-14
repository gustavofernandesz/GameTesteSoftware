package st.project.game.system.screens;

import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;

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
}
