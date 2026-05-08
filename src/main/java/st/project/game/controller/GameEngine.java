package st.project.game.controller;

import st.project.game.model.GameModel;

import javax.swing.Timer;

/**
 * Controlador que orquestra o ciclo de vida do jogo.
 * Mantém uma referência ao modelo e ao timer, e expõe o método "mover"
 * para a View. Não armazena mais estado do jogo.
 */
public class GameEngine {
    private final GameModel model;
    private Timer timer;

    public GameEngine(GameModel model) {
        this.model = model;
        iniciarTimer();
    }

    private void iniciarTimer() {
        timer = new Timer(1000, e -> {
            if (model.isJogoAtivo()) {
                model.reduzirTempo();
            } else {
                timer.stop();
            }
        });
        timer.start();
    }

    /**
     * Comando de movimento vindo da View.
     * @return true se o movimento foi bem sucedido, false caso contrário.
     */
    public boolean mover(String direcao) {
        boolean moveu = model.moverJogador(direcao);
        if (moveu) {
            // Após o movimento, verifica condições de fim de jogo
            if (model.getMissao().isMissaoConcluida()) {
                model.finalizarJogo(true);
            } else if (model.getMovimentosRestantes() <= 0) {
                model.finalizarJogo(false);
            }
        }
        return moveu;
    }
}