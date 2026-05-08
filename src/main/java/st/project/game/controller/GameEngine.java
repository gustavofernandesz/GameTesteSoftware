package st.project.game.controller;

import st.project.game.model.GameModel;

import javax.swing.Timer;

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

    public boolean mover(String direcao) {
        boolean moveu = model.moverJogador(direcao);
        if (moveu) {
            if (model.getMissao().isMissaoConcluida()) {
                model.finalizarJogo(true);
            } else if (model.getMovimentosRestantes() <= 0) {
                model.finalizarJogo(false);
            }
        }
        return moveu;
    }
}