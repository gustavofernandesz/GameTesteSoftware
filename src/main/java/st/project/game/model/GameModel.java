package st.project.game.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class GameModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Map<String, Room> salas;
    private final Player jogador;
    private final Mission missao;
    private int tempoRestante;
    private int movimentosRestantes;
    private boolean jogoAtivo;

    // transient – não será salvo
    private transient PropertyChangeSupport pcs;

    private static final int MAX_MOVIMENTOS = 7;

    private final long seed;
    private transient Random random;

    public GameModel() {
        this(System.currentTimeMillis());
    }

    public GameModel(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
        this.pcs = new PropertyChangeSupport(this);
        jogoAtivo = true;
        inicializarMapa(random);
        inicializarItens();
        this.jogador = new Player(salas.get("entrada"));
        this.missao = new Mission(salas.get("sagrado"));
        this.tempoRestante = 60;
        this.movimentosRestantes = MAX_MOVIMENTOS;
    }

    // Método chamado automaticamente durante a desserialização
    @Serial
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.random = new Random(seed);
        this.pcs = new PropertyChangeSupport(this);   // recria vazio
    }

    public Map<String, Room> getSalas() { return salas; }
    public Player getJogador() { return jogador; }
    public Mission getMissao() { return missao; }
    public int getTempoRestante() { return tempoRestante; }
    public int getMovimentosRestantes() { return movimentosRestantes; }
    public boolean isJogoAtivo() { return jogoAtivo; }
    public boolean isChaveAtiva() { return jogador.possuiItem(Item.Type.CHAVE); }

    public int getScore() {
        return tempoRestante * 10 + movimentosRestantes * 5 + jogador.getInventario().size() * 100;
    }

    public int getNivel() {
        return 1 + (int) jogador.getInventario().stream()
                .filter(i -> i.getTipo() != Item.Type.CHAVE)
                .count();
    }

    public boolean moverJogador(String direcao) {
        if (!jogoAtivo || movimentosRestantes <= 0) return false;

        Room atual = jogador.getPosicaoAtual();
        Room destino = atual.getVizinho(direcao);
        if (destino == null) return false;

        int oldScore = getScore();
        int oldNivel = getNivel();

        boolean moveu = jogador.moverPara(destino);
        if (moveu) {
            int oldMov = movimentosRestantes;
            movimentosRestantes--;
            pcs.firePropertyChange("movimentos", oldMov, movimentosRestantes);

            coletarItensSala();
            missao.verificarProgresso(jogador);

            pcs.firePropertyChange("score", oldScore, getScore());
            pcs.firePropertyChange("nivel", oldNivel, getNivel());
        }
        return moveu;
    }

    public void reduzirTempo() {
        if (!jogoAtivo) return;
        int old = tempoRestante;
        int oldScore = getScore();
        tempoRestante--;
        pcs.firePropertyChange("tempo", old, tempoRestante);
        pcs.firePropertyChange("score", oldScore, getScore());
        if (tempoRestante <= 0) {
            finalizarJogo(false);
        }
    }

    public void finalizarJogo(boolean vitoria) {
        if (!jogoAtivo) return;
        jogoAtivo = false;
        pcs.firePropertyChange("gameOver", null, vitoria);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

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
                break;
        }
    }

    private void inicializarMapa(Random rnd) {
        salas = new HashMap<>();
        List<String> nomesIntermedios = new ArrayList<>(Arrays.asList(
                "sala1","sala2","sala3","sala4","corredor","biblioteca","sala5","sala6","sala7",
                "jardim","cozinha","sala8","sala9","sala10","torre","sala11","sala12","sala13","sala14",
                "sala15","sala16","sala17","sala18"
        ));
        Collections.shuffle(nomesIntermedios, rnd);

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