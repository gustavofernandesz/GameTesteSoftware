package st.project.game.controller;

import st.project.game.model.GameModel;

import javax.swing.Timer;

public class GameEngine {
    private final GameModel model;
    private Timer timer;
    private boolean jogoEncerrado = false;

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

    public void encerrarJogo() {
        jogoEncerrado = true;
        timer.stop();
    }


    public void pausar() {
        timer.stop();
    }

    public void retomar() {
        if (!jogoEncerrado) {
            timer.start();
        }
    }

    public boolean isJogoEncerrado() {
        return jogoEncerrado;
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