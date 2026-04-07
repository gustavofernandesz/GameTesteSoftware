package st.project.game;

import javax.swing.Timer;
import java.util.*;

public class GameEngine {
    private Map<String, Room> salas;          // mapeamento nome -> Room
    private final Player jogador;
    private final Mission missao;
    private int tempoRestante;                 // segundos
    private Timer timer;
    private boolean jogoAtivo;
    private final TimerListener timerListener;
    private static final int MAX_MOVIMENTOS = 7;
    private int movimentosRestantes = MAX_MOVIMENTOS;


    public interface TimerListener {
        void onTempoAtualizado(int segundosRestantes);
        void onJogoTerminado(boolean vitoria);
        void onMovimentoRealizado(int movRestantes);
    }

    public GameEngine(TimerListener listener) {
        this.timerListener = listener;
        this.jogoAtivo = true;
        inicializarMapa();
        inicializarItens();
        this.jogador = new Player(salas.get("entrada"));
        this.missao = new Mission(salas.get("sagrado"));
        this.tempoRestante = 60;
        iniciarTimer();
    }
    public GameEngine(TimerListener listener, Random seed) {
        this.timerListener = listener;
        this.jogoAtivo = true;
        inicializarMapa(seed);
        inicializarItens();
        this.jogador = new Player(salas.get("entrada"));
        this.missao = new Mission(salas.get("sagrado"));
        this.tempoRestante = 60;
        iniciarTimer();
    }

    private void inicializarMapa() {
        inicializarMapa(new Random());
    }
    private void inicializarMapa(Random seed) {
        salas = new HashMap<>();
        // Nomes das salas intermediárias (tudo exceto "entrada" e "sagrado")
        List<String> nomesIntermedios = new ArrayList<>(Arrays.asList(
                "sala1", "sala2", "sala3", "sala4",
                "corredor", "biblioteca", "sala5", "sala6", "sala7",
                "jardim", "cozinha", "sala8", "sala9", "sala10",
                "torre", "sala11", "sala12", "sala13", "sala14",
                "sala15", "sala16", "sala17", "sala18"


        ));

        // Embaralha apenas as salas do meio
        Collections.shuffle(nomesIntermedios, seed);

        // Monta o array final: entrada fixa na pos 0 (x=0,y=0),
        // sagrado fixo na pos 24 (x=4,y=4)
        String[] nomes = new String[25];
        nomes[0]  = "entrada";
        nomes[24] = "sagrado";
        for (int i = 0; i < nomesIntermedios.size(); i++) {
            nomes[i + 1] = nomesIntermedios.get(i);
        }

        // Criar todas as salas com coordenadas de grid (x=coluna, y=linha)
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                int idx = i * 5 + j;
                Room r = new Room(nomes[idx], j, i);
                salas.put(nomes[idx], r);
            }
        }


        // Configurar adjacências (4 direções)
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
        // Bloquear sala do cálice
        salas.get("sagrado").setBloqueada(true);
    }

    private void inicializarItens() {
        // Colocar itens em salas específicas
        salas.get("biblioteca").adicionarItem(new Item("Chave Encantada", Item.Type.CHAVE, "Abre a sala do cálice"));
        salas.get("cozinha").adicionarItem(new Item("Poção de Velocidade", Item.Type.POCAO_VELOCIDADE, "Dobra o tempo restante"));
        salas.get("jardim").adicionarItem(new Item("Amuleto de Movimentos", Item.Type.AMULETO_VISAO, "Aumenta seus movimentos em 3"));
        salas.get("sagrado").adicionarItem(new Item("Cálice Mágico", Item.Type.CALICE, "O objeto da missão"));
    }

    private void iniciarTimer() {
        timer = new Timer(1000, e -> {
            if (jogoAtivo) {
                tempoRestante--;
                if (timerListener != null)
                    timerListener.onTempoAtualizado(tempoRestante);
                if (tempoRestante <= 0) {
                    jogoAtivo = false;
                    timer.stop();
                    if (timerListener != null)
                        timerListener.onJogoTerminado(false);
                }
            }
        });
        timer.start();
    }

    public boolean moverJogador(String direcao) {
        if (!jogoAtivo) return false;

        // Verifica limite de movimentos antes de tentar mover
        if (movimentosRestantes <= 0) {
            encerrarJogo(false);
            return false;
        }

        Room atual = jogador.getPosicaoAtual();
        Room destino = atual.getVizinho(direcao);
        if (destino == null) return false;
        boolean moveu = jogador.moverPara(destino);
        if (moveu) {
            movimentosRestantes--;
            if (timerListener != null)
                timerListener.onMovimentoRealizado(movimentosRestantes); // sem cast
            coletarItensSala();
            // Verificar missão
            missao.verificarProgresso(jogador);
            if (missao.isMissaoConcluida()) {
                encerrarJogo(true);
                return true;
            }
            if (movimentosRestantes <= 0) {
                encerrarJogo(false);
            }
        }
        return moveu;
    }
    private void encerrarJogo(boolean vitoria) {
        jogoAtivo = false;
        timer.stop();
        if (timerListener != null)
            timerListener.onJogoTerminado(vitoria);
    }

    public void coletarItensSala() {
        Room atual = jogador.getPosicaoAtual();
        List<Item> itens = new ArrayList<>(atual.getItems());
        for (Item item : itens) {
            // Coleta automática
            atual.removerItem(item);
            jogador.adicionarItem(item);
            // Aplicar efeitos imediatos
            aplicarEfeitoItem(item);
        }
    }

    private void aplicarEfeitoItem(Item item) {
        switch (item.getTipo()) {
            case POCAO_VELOCIDADE:
                tempoRestante *= 2;
                if (timerListener != null)
                    timerListener.onTempoAtualizado(tempoRestante);
                break;
            case AMULETO_VISAO:
                movimentosRestantes += 3;
                if (timerListener != null)
                    timerListener.onMovimentoRealizado(movimentosRestantes);

                break;
            case CHAVE:
                // A chave é usada automaticamente para destrancar a sala do cálice
                // quando o jogador tentar entrar
                // Como a sala está bloqueada, a chave só é verificada no métod moverPara
                break;
            case CALICE:
                // Missão será concluída na verificação
                break;
        }
    }

    public Player getJogador() { return jogador; }
    public Mission getMissao() { return missao; }
    public Map<String, Room> getSalas() { return salas; }

    public boolean isJogoAtivo() { return jogoAtivo; }
    public void setJogoAtivo(boolean ativo){this.jogoAtivo = ativo;}

    public boolean isChaveAtiva() { return jogador.possuiItem(Item.Type.CHAVE); }

    public int getTempoRestante() { return tempoRestante; }
    public void setTempoRestante(int tempo){this.tempoRestante = tempo;}

    public int getMovimentosRestantes(){return this.movimentosRestantes;}
    public void setMovimentosRestantes(int movimentos){this.movimentosRestantes = movimentos;    }
}
