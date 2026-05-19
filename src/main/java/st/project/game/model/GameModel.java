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

    private static final int MAX_MOVIMENTOS = 200;
    private static final int MAX_ANDARES    = 4;

    private Room salaDaChave;
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
        this.tempoRestante = 120;
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


    public int getAndarAtual()                      { return jogador.getPosicaoAtual().getAndar(); }
    public boolean isChaveVisivel()                 { return jogador.possuiItem(Item.Type.LUPA); }
    public Room getSalaDaChave()                    { return salaDaChave; }

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
        int oldAndar = getAndarAtual();

        boolean moveu = jogador.moverPara(destino);
        if (moveu) {
            int oldMov = movimentosRestantes;
            movimentosRestantes--;
            pcs.firePropertyChange("movimentos", oldMov, movimentosRestantes);

            //------------------------------------------------------------------------------------------
            //---------------------------Adição de teleporte automático de escada --------------------
            // Ao entrar em uma sala escada_cima ou escada_baixo, o jogador é teleportado
            // automaticamente para a escada correspondente do andar acima/abaixo.
            // Dispara também o evento "andar" para atualizar o label na GUI.
            //-----------------------------------------------------------------------------------------
            //-----------------------------------------------------------------------------------------
            // ── Teleporte automático de escada ─────────────────────────────
            // escadaCima  → busca qualquer escadaBaixo no andar acima
            // escadaBaixo → busca qualquer escadaCima  no andar abaixo
            // Tipos opostos garantem que a chegada nunca dispara novo teleporte.
            Room posAtual = jogador.getPosicaoAtual();
            if (posAtual.isEscadaCima()) {
                Room destEscada = getEscadaNoAndar(posAtual.getAndar() + 1, false);
                if (destEscada != null) {
                    jogador.moverPara(destEscada);
                }
            } else if (posAtual.isEscadaBaixo()) {
                Room destEscada = getEscadaNoAndar(posAtual.getAndar() - 1, true);
                if (destEscada != null) {
                    jogador.moverPara(destEscada);
                }
            }
            // ──────────────────────────────────────────────────────────────

            coletarItensSala();
            missao.verificarProgresso(jogador);

            pcs.firePropertyChange("score", oldScore, getScore());
            pcs.firePropertyChange("nivel", oldNivel, getNivel());

            int novoAndar = getAndarAtual();
            if (novoAndar != oldAndar) {
                pcs.firePropertyChange("andar", oldAndar, novoAndar);
            }
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
            if (item.getTipo() != Item.Type.LUPA && !jogador.possuiItem(Item.Type.LUPA)) {
                continue; // item invisível/intocável sem a lupa
            }
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

            case LUPA:
                pcs.firePropertyChange("lupaObtida", false, true);
                break;
            case CHAVE:
            case CALICE:
                break;
        }
    }

    private void inicializarMapa(Random rnd) {
        salas = new HashMap<>();
        List<String> nomesA1 = new ArrayList<>(Arrays.asList(
                "sala1", "sala2", "sala3", "sala4", "corredor", "biblioteca", "sala5", "sala6", "sala7",
                "jardim", "cozinha", "sala8", "sala9", "sala10", "torre", "sala11", "sala12", "sala13", "sala14",
                "sala15", "sala16", "sala17", "sala18"
        ));
        Collections.shuffle(nomesA1, rnd);

        List<String> nomesA2 = new ArrayList<>(Arrays.asList(
                "laboratorio", "deposito", "arquivo", "almoxarifado", "oficina",
                "geladeira", "caldeira", "relogio", "engrenagem", "tubulacao",
                "subestacao", "forja", "arsenal", "camara", "cripta",
                "passagem", "galeria", "corredor2", "vestibulo", "antecamara",
                "celula", "gaiola", "porao"
        ));
        Collections.shuffle(nomesA2, rnd);

        List<String> nomesA3 = new ArrayList<>(Arrays.asList(
                "salao", "trono", "capelao", "relicario", "observatorio",
                "planetario", "astrolabio", "oraculo", "nexo", "portal",
                "cristal", "vitral", "altar", "fonte", "espelho",
                "labirinto", "santuario", "obelisco", "tumulto", "catacumba",
                "abismo", "penumbra", "limiar"
        ));
        Collections.shuffle(nomesA3, rnd);

        List<String> nomesA4 = new ArrayList<>(Arrays.asList(
                "caminho", "refuga", "ultima", "antessala", "fortaleza",
                "baluarte", "cidadela", "vigia", "sentinela",
                "guardiao", "protetor", "defensor", "guarida", "covil",
                "esconderijo", "toca", "abrigo", "retiro", "clausura",
                "reclusao", "isolamento", "exilio", "ostracismo"
        ));
        Collections.shuffle(nomesA4, rnd);


        // Andar 1: "entrada" em (0,0), escada_cima_1 em (4,0), SEM escada_baixo
        criarAndar(1, "entrada", null, "escada_cima_1", null, nomesA1);

        // Andar 2: escada_cima_2 em (4,0), escada_baixo_2 em (0,4)
        criarAndar(2, null, null, "escada_cima_2", "escada_baixo_2", nomesA2);

        // Andar 3: escada_cima_3 em (4,0), escada_baixo_3 em (0,4)
        criarAndar(3, null, null, "escada_cima_3", "escada_baixo_3", nomesA3);

        // Andar 4: "sagrado" em (4,4), SEM escada_cima, escada_baixo_4 em (0,4)
        criarAndar(4, null, "sagrado", null, "escada_baixo_4", nomesA4);

        //conexões internas de N/S/L/O
        for (int andar = 1; andar <= MAX_ANDARES; andar++) {
            conectarAndar(andar);
        }
        salas.get("sagrado").setBloqueada(true);

    }

    private void criarAndar(int andar,
                            String nomeFixo00, String nomeFixo44,
                            String nomeEscadaCima, String nomeEscadaBaixo,
                            List<String> nomesIntermedios) {
        String[] nomes = new String[25];
        // índice 0 = (col=0,row=0), índice 4 = (col=4,row=0),
        // índice 20 = (col=0,row=4), índice 24 = (col=4,row=4)
        if (nomeFixo00    != null) nomes[0]  = nomeFixo00;
        if (nomeFixo44    != null) nomes[24] = nomeFixo44;
        if (nomeEscadaCima  != null) nomes[4]  = nomeEscadaCima;   // (col=4, row=0)
        if (nomeEscadaBaixo != null) nomes[20] = nomeEscadaBaixo;  // (col=0, row=4)

        int cursor = 0;
        for (int i = 0; i < 25; i++) {
            if (nomes[i] != null) continue;
            if (cursor < nomesIntermedios.size()) {
                nomes[i] = nomesIntermedios.get(cursor++);
            } else {
                nomes[i] = "sala_" + andar + "_" + i;
            }
        }

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                int idx = row * 5 + col;
                Room r = new Room(nomes[idx], col, row, andar);
                salas.put(nomes[idx], r);
            }
        }
    }

    private void conectarAndar(int andar) {
        for (Room r : salas.values()) {
            if (r.getAndar() != andar) continue;
            int col = r.getX(), row = r.getY();
            Room norte = getRoomPorAndarXY(andar, col,     row - 1);
            Room sul   = getRoomPorAndarXY(andar, col,     row + 1);
            Room oeste = getRoomPorAndarXY(andar, col - 1, row);
            Room leste = getRoomPorAndarXY(andar, col + 1, row);
            if (norte != null) r.setVizinho("norte", norte);
            if (sul   != null) r.setVizinho("sul",   sul);
            if (oeste != null) r.setVizinho("oeste", oeste);
            if (leste != null) r.setVizinho("leste", leste);
        }
    }

    private Room getEscadaNoAndar(int andar, boolean isCima) {
        for (Room r : salas.values()) {
            if (r.getAndar() == andar) {
                if (isCima  && r.isEscadaCima())  return r;
                if (!isCima && r.isEscadaBaixo()) return r;
            }
        }
        return null;
    }


    private Room getRoomPorAndarXY(int andar, int col, int row) {
        if (col < 0 || col > 4 || row < 0 || row > 4) return null;
        for (Room r : salas.values()) {
            if (r.getAndar() == andar && r.getX() == col && r.getY() == row) return r;
        }
        return null;
    }

    private void inicializarItens() {
        // Lupa — andar 2, posição (2,2)
        Room salaLupa = getRoomPorAndarXY(2, 2, 2);
        if (salaLupa != null) {
            salaLupa.adicionarItem(new Item("Lupa Arcana", Item.Type.LUPA,
                    "Revela itens ocultos ao seu redor"));
        }

        // Chave — andar 3, posição (1,1) — visível só com a lupa
        salaDaChave = getRoomPorAndarXY(3, 1, 1);
        if (salaDaChave != null) {
            salaDaChave.adicionarItem(new Item("Chave Encantada", Item.Type.CHAVE,
                    "Abre a sala do cálice"));
        }

        // Poção de Velocidade — andar 1, posição (2,3)
        Room salaPocao = getRoomPorAndarXY(1, 2, 3);
        if (salaPocao != null) {
            salaPocao.adicionarItem(new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE,
                    "Dobra o tempo restante"));
        }

        // Amuleto de Movimentos — andar 2, posição (3,2)
        Room salaAmuleto = getRoomPorAndarXY(2, 3, 2);
        if (salaAmuleto != null) {
            salaAmuleto.adicionarItem(new Item("Amuleto de Movimentos", Item.Type.AMULETO_VISAO,
                    "Aumenta seus movimentos em 100"));
        }

        // Cálice — andar 4, dentro de "sagrado" (bloqueada)
        salas.get("sagrado").adicionarItem(new Item("Cálice Mágico", Item.Type.CALICE,
                "O objeto da missão"));
    }
}