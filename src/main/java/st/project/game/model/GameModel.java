package st.project.game.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

/**
 * Modelo centralizado do jogo. Encapsula todo o estado e as regras
 * de negócio (movimentação, coleta de itens, efeitos, progresso da missão).
 * Notifica a View através de PropertyChangeSupport.
 */
public class GameModel {
    private Map<String, Room> salas;
    private final Player jogador;
    private final Mission missao;
    private int tempoRestante;
    private int movimentosRestantes;
    private boolean jogoAtivo;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private static final int MAX_MOVIMENTOS = 7;

    // ── Construtores ────────────────────────────────────────────────
    public GameModel() {
        this(new Random());
    }

    public GameModel(Random seed) {
        jogoAtivo = true;
        inicializarMapa(seed);
        inicializarItens();
        this.jogador = new Player(salas.get("entrada"));
        this.missao = new Mission(salas.get("sagrado"));
        this.tempoRestante = 60;
        this.movimentosRestantes = MAX_MOVIMENTOS;
    }

    // ── Getters (estado apenas leitura) ────────────────────────────
    public Map<String, Room> getSalas() { return salas; }
    public Player getJogador() { return jogador; }
    public Mission getMissao() { return missao; }
    public int getTempoRestante() { return tempoRestante; }
    public int getMovimentosRestantes() { return movimentosRestantes; }
    public boolean isJogoAtivo() { return jogoAtivo; }
    public boolean isChaveAtiva() { return jogador.possuiItem(Item.Type.CHAVE); }

    // ── Métodos de manipulação de estado ─────────────────────────────

    /** Tenta mover o jogador na direção informada. Retorna true se moveu. */
    public boolean moverJogador(String direcao) {
        if (!jogoAtivo || movimentosRestantes <= 0) return false;

        Room atual = jogador.getPosicaoAtual();
        Room destino = atual.getVizinho(direcao);
        if (destino == null) return false;

        boolean moveu = jogador.moverPara(destino);
        if (moveu) {
            int oldMov = movimentosRestantes;
            movimentosRestantes--;
            pcs.firePropertyChange("movimentos", oldMov, movimentosRestantes);

            coletarItensSala();
            missao.verificarProgresso(jogador);
        }
        return moveu;
    }

    /** Decrementa o tempo do jogo (chamado pelo timer). */
    public void reduzirTempo() {
        if (!jogoAtivo) return;
        int old = tempoRestante;
        tempoRestante--;
        pcs.firePropertyChange("tempo", old, tempoRestante);
        if (tempoRestante <= 0) {
            finalizarJogo(false);
        }
    }

    /** Encerra o jogo (vitória ou derrota) e notifica a View. */
    public void finalizarJogo(boolean vitoria) {
        if (!jogoAtivo) return;   // evita múltiplas notificações
        jogoAtivo = false;
        pcs.firePropertyChange("gameOver", null, vitoria);
    }

    // ── Observer (PropertyChangeListener) ──────────────────────────
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    // ── Métodos privados de lógica interna ──────────────────────────

    private void coletarItensSala() {
        Room atual = jogador.getPosicaoAtual();
        List<Item> itens = new ArrayList<>(atual.getItems());
        for (Item item : itens) {
            atual.removerItem(item);
            jogador.adicionarItem(item);
            aplicarEfeitoItem(item);
        }
    }

    private void aplicarEfeitoItem(Item item) {
        switch (item.getTipo()) {
            case POCAO_VELOCIDADE:
                int oldT = tempoRestante;
                tempoRestante *= 2;
                pcs.firePropertyChange("tempo", oldT, tempoRestante);
                break;
            case AMULETO_VISAO:
                int oldM = movimentosRestantes;
                movimentosRestantes += 3;
                pcs.firePropertyChange("movimentos", oldM, movimentosRestantes);
                break;
            case CHAVE:
            case CALICE:
                // Efeitos indiretos – tratados em Player.moverPara() e Mission.verificarProgresso()
                break;
        }
    }

    // ── Inicialização do mapa e itens (igual ao GameEngine original) ──

    private void inicializarMapa(Random seed) {
        salas = new HashMap<>();
        List<String> nomesIntermedios = new ArrayList<>(Arrays.asList(
                "sala1","sala2","sala3","sala4","corredor","biblioteca","sala5","sala6","sala7",
                "jardim","cozinha","sala8","sala9","sala10","torre","sala11","sala12","sala13","sala14",
                "sala15","sala16","sala17","sala18"
        ));
        Collections.shuffle(nomesIntermedios, seed);

        String[] nomes = new String[25];
        nomes[0] = "entrada";
        nomes[24] = "sagrado";
        for (int i = 0; i < nomesIntermedios.size(); i++) {
            nomes[i + 1] = nomesIntermedios.get(i);
        }

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int idx = i * 5 + j;
                Room r = new Room(nomes[idx], j, i);
                salas.put(nomes[idx], r);
            }
        }

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int idx = i * 5 + j;
                Room r = salas.get(nomes[idx]);
                if (i > 0) r.setVizinho("norte", salas.get(nomes[(i - 1) * 5 + j]));
                if (i < 4) r.setVizinho("sul",   salas.get(nomes[(i + 1) * 5 + j]));
                if (j > 0) r.setVizinho("oeste",  salas.get(nomes[i * 5 + (j - 1)]));
                if (j < 4) r.setVizinho("leste",  salas.get(nomes[i * 5 + (j + 1)]));
            }
        }
        salas.get("sagrado").setBloqueada(true);
    }

    private void inicializarItens() {
        salas.get("biblioteca").adicionarItem(new Item("Chave Encantada", Item.Type.CHAVE, "Abre a sala do cálice"));
        salas.get("cozinha").adicionarItem(new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante"));
        salas.get("jardim").adicionarItem(new Item("Amuleto de Movimentos", Item.Type.AMULETO_VISAO, "Aumenta seus movimentos em 3"));
        salas.get("sagrado").adicionarItem(new Item("Cálice Mágico", Item.Type.CALICE, "O objeto da missão"));
    }
}