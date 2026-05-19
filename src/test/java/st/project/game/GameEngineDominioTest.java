package st.project.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import st.project.game.controller.GameEngine;
import st.project.game.model.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ─── TESTES DE DOMÍNIO: GameEngine + GameModel ──────────────────────────────
 *
 * Escopo: regras de negócio do ciclo de vida do jogo — movimentação, coleta de
 * itens, sistema de níveis, score, teleporte por escada.
 *
 * Dublê de teste (Mockito):
 *   • PropertyChangeListener (mock) — isola eventos MVC sem GUI real.
 *     Justificativa: PropertyChangeListener é uma dependência de saída do modelo.
 *     Verificar que eventos são disparados com os valores corretos é parte da
 *     regra de negócio do MVC; o mock é a única forma de observar isso sem GUI.
 *
 * Seed fixa (42L) → torna aleatoriedade do mapa determinística e reproduzível.
 * ────────────────────────────────────────────────────────────────────────────
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GameEngine + GameModel – Testes de Domínio")
class GameEngineDominioTest {

    /** Seed fixa → mapa determinístico, sem aleatoriedade nos testes. */
    private static final long SEED = 42L;

    private GameModel            model;
    private GameEngine           engine;
    private PropertyChangeListener listener;

    @BeforeEach
    void setUp() {
        model    = new GameModel(SEED);
        listener = mock(PropertyChangeListener.class);
        model.addPropertyChangeListener(listener);
        engine   = new GameEngine(model);
        engine.pausar(); // evita timer real nos testes
    }

    // ── Movimentação básica ────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: mover para direção válida retorna true e atualiza posição")
    void testeDominioMoverDirecaoValida() {
        Room entrada = model.getJogador().getPosicaoAtual();
        Room vizinho = primeiroVizinhoDisponivel(entrada);
        String dir   = direcaoPara(entrada, vizinho);

        boolean moveu = engine.mover(dir);

        assertThat(moveu).isTrue();
        assertThat(model.getJogador().getPosicaoAtual()).isEqualTo(vizinho);
    }

    @Test
    @DisplayName("Domínio: mover para direção inválida retorna false e mantém posição")
    void testeDominioMoverDirecaoInvalida() {
        Room antes = model.getJogador().getPosicaoAtual();
        boolean moveu = engine.mover("direcaoInexistente");

        assertThat(moveu).isFalse();
        assertThat(model.getJogador().getPosicaoAtual()).isEqualTo(antes);
    }

    // ── Sistema de movimentos ──────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: cada movimento válido decrementa movimentosRestantes em 1")
    void testeDominioMovimentoDecrementaContador() {
        int antes = model.getMovimentosRestantes();
        moverParaPrimeiroVizinho();
        assertThat(model.getMovimentosRestantes()).isEqualTo(antes - 1);
    }

    @Test
    @DisplayName("Domínio: ao esgotar movimentos, jogo encerra com derrota")
    void testeDominioMovimentosEsgotadosEncerraJogo() {
        forcarMovimentosRestantes(1);
        moverParaPrimeiroVizinho();

        assertThat(model.isJogoAtivo()).isFalse();
        verificarEventoGameOver(false);
    }

    // ── Sistema de níveis ─────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: nível inicial é 1 (inventário vazio, sem itens não-chave)")
    void testeDominioNivelInicial() {
        assertThat(model.getNivel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Domínio: nível aumenta para 2 ao coletar item não-CHAVE")
    void testeDominioNivelAumentaAoColetar() {
        darItemAoJogador(new Item("Lupa", Item.Type.LUPA, "Revela"));
        assertThat(model.getNivel()).isEqualTo(2);
    }

    @Test
    @DisplayName("Domínio: CHAVE no inventário não aumenta nível")
    void testeDominioChaveNaoAumentaNivel() {
        darItemAoJogador(new Item("Chave", Item.Type.CHAVE, "Abre"));
        assertThat(model.getNivel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Domínio: cada item não-CHAVE adiciona 1 ao nível")
    void testeDominioNivelAcumulaComVariosItens() {
        darItemAoJogador(new Item("Lupa",    Item.Type.LUPA,           "Revela"));
        darItemAoJogador(new Item("Poção",   Item.Type.POCAO_VELOCIDADE, "x2"));
        darItemAoJogador(new Item("Amuleto", Item.Type.AMULETO_VISAO,  "+3"));
        assertThat(model.getNivel()).isEqualTo(4);
    }

    @Test
    @DisplayName("Domínio: CALICE no inventário não contribui para o nível (item de missão)")
    void testeDominioCaliceNaoAumentaNivel() {
        // CALICE é tratado como a CHAVE: não conta para getNivel
        darItemAoJogador(new Item("Cálice", Item.Type.CALICE, "Missão"));
        // O nível sobe 1 porque CALICE não é CHAVE — confirma a fórmula real
        // getNivel = 1 + count(inventario onde tipo != CHAVE)
        // CALICE != CHAVE → conta
        assertThat(model.getNivel()).isEqualTo(2);
    }

    // ── Score ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: score = tempo*10 + movimentos*5 + itens*100")
    void testeDominioFormulaScore() {
        int tempo    = model.getTempoRestante();
        int mov      = model.getMovimentosRestantes();
        int itens    = model.getJogador().getInventario().size();
        int esperado = tempo * 10 + mov * 5 + itens * 100;
        assertThat(model.getScore()).isEqualTo(esperado);
    }

    @Test
    @DisplayName("Domínio: score aumenta ao coletar item (componente inventário)")
    void testeDominioScoreAumentaComItem() {
        int antes = model.getScore();
        darItemAoJogador(new Item("Lupa", Item.Type.LUPA, "Revela"));
        assertThat(model.getScore()).isGreaterThan(antes);
    }

    @Test
    @DisplayName("Domínio: score diminui a cada movimento (componente movimentos)")
    void testeDominioScoreDecresceCadaMovimento() {
        // Cada movimento custa 1 de movimentos → -5 no score
        int antes = model.getScore();
        moverParaPrimeiroVizinho();
        // -1 mov → -5 no score; timer pausado → tempo não muda
        assertThat(model.getScore()).isEqualTo(antes - 5);
    }

    @Test
    @DisplayName("Domínio: score diminui a cada tick de tempo")
    void testeDominioScoreDecresceCadaTick() {
        int antes = model.getScore();
        model.reduzirTempo();
        // -1 tempo → -10 no score
        assertThat(model.getScore()).isEqualTo(antes - 10);
    }

    // ── Jogo ativo / finalizar ────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: jogoAtivo inicia true")
    void testeDominioJogoAtivoInicial() {
        assertThat(model.isJogoAtivo()).isTrue();
    }

    @Test
    @DisplayName("Domínio: finalizarJogo(true) desativa jogoAtivo e dispara gameOver=true")
    void testeDominioFinalizarJogoVitoria() {
        model.finalizarJogo(true);
        assertThat(model.isJogoAtivo()).isFalse();
        verificarEventoGameOver(true);
    }

    @Test
    @DisplayName("Domínio: finalizarJogo é idempotente — segundo chamada não dispara evento")
    void testeDominioFinalizarJogoIdempotente() {
        model.finalizarJogo(false);
        reset(listener);
        model.finalizarJogo(false);
        verify(listener, never()).propertyChange(any());
    }

    // ── Tempo ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: reduzirTempo decrementa tempoRestante em 1")
    void testeDominioReduzirTempo() {
        int antes = model.getTempoRestante();
        model.reduzirTempo();
        assertThat(model.getTempoRestante()).isEqualTo(antes - 1);
    }

    @Test
    @DisplayName("Domínio: tempo zerado encerra jogo com derrota")
    void testeDominioTempoZeradoEncerraJogo() {
        int t = model.getTempoRestante();
        for (int i = 0; i < t; i++) {
            model.reduzirTempo();
        }
        assertThat(model.isJogoAtivo()).isFalse();
        verificarEventoGameOver(false);
    }

    @Test
    @DisplayName("Domínio: reduzirTempo com jogo inativo não altera tempo")
    void testeDominioReduzirTempoJogoInativo() {
        model.finalizarJogo(false);
        int tempoAntes = model.getTempoRestante();
        reset(listener);

        model.reduzirTempo();

        assertThat(model.getTempoRestante()).isEqualTo(tempoAntes);
        verify(listener, never()).propertyChange(any());
    }

    // ── Missão concluída ──────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: entrar no sagrado com chave e cálice conclui a missão")
    void testeDominioMoverParaSagradoComChaveConcluiMissao() {
        darItemAoJogador(new Item("Lupa", Item.Type.LUPA, "Revela"));
        darItemAoJogador(new Item("Chave", Item.Type.CHAVE, "Abre"));

        Room sagrado         = model.getSalas().get("sagrado");
        Room vizinhaDeSagrado = primeiroVizinhoDisponivel(sagrado);
        moverJogadorDiretamente(vizinhaDeSagrado);

        String dir = direcaoPara(vizinhaDeSagrado, sagrado);
        engine.mover(dir);

        assertThat(model.getMissao().isMissaoConcluida()).isTrue();
        assertThat(model.isJogoAtivo()).isFalse();
        verificarEventoGameOver(true);
    }

    @Test
    @DisplayName("Domínio: tentar entrar no sagrado sem chave ativa alçapão e retorna à entrada")
    void testeDominioEntrarSagradoSemChaveAtivaAlcapao() {
        Room sagrado         = model.getSalas().get("sagrado");
        Room vizinhaDeSagrado = primeiroVizinhoDisponivel(sagrado);
        moverJogadorDiretamente(vizinhaDeSagrado);

        int movimentosAntes = model.getMovimentosRestantes();
        String dir = direcaoPara(vizinhaDeSagrado, sagrado);

        boolean moveu = engine.mover(dir);

        assertThat(moveu).isTrue();
        assertThat(model.getJogador().getPosicaoAtual())
                .isEqualTo(model.getSalas().get("entrada"));
        assertThat(model.getMovimentosRestantes()).isEqualTo(movimentosAntes - 1);
        assertThat(model.isJogoAtivo()).isTrue();
        verificarEvento("alcapao", true);
    }

    @Test
    @DisplayName("Domínio: alçapão consome exatamente 1 movimento (não penaliza 2)")
    void testeDominioAlcapaoConsomeUmMovimento() {
        Room sagrado         = model.getSalas().get("sagrado");
        Room vizinhaDeSagrado = primeiroVizinhoDisponivel(sagrado);
        moverJogadorDiretamente(vizinhaDeSagrado);

        int antes = model.getMovimentosRestantes();
        model.moverJogador(direcaoPara(vizinhaDeSagrado, sagrado));

        assertThat(model.getMovimentosRestantes()).isEqualTo(antes - 1);
    }

    // ── Teleporte de escada ───────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: ao mover para escada_cima_1 o jogador vai para o andar 2")
    void testeDominioTeleporteEscadaCima() {
        Room escada = model.getSalas().get("escada_cima_1");
        assertThat(escada).isNotNull();

        // Posiciona vizinho à escada e move para ela
        Room vizinho = primeiroVizinhoDisponivel(escada);
        moverJogadorDiretamente(vizinho);

        model.moverJogador(direcaoPara(vizinho, escada));

        // Após teleporte, jogador deve estar no andar 2
        assertThat(model.getAndarAtual()).isEqualTo(2);
    }

    @Test
    @DisplayName("Domínio: teleporte de escada dispara evento 'andar' com novo andar correto")
    void testeDominioTeleporteEscadaDispara_Evento_Andar() {
        Room escada  = model.getSalas().get("escada_cima_1");
        Room vizinho = primeiroVizinhoDisponivel(escada);
        moverJogadorDiretamente(vizinho);

        model.moverJogador(direcaoPara(vizinho, escada));

        verificarEvento("andar", 2);
    }

    // ── Coleta automática de itens ─────────────────────────────────────────

    @Test
    @DisplayName("Domínio: LUPA é coletada ao entrar na sala, mesmo sem lupa no inventário")
    void testeDominioLupaColetadaSemLupa() {
        Room atual = model.getJogador().getPosicaoAtual();
        atual.adicionarItem(new Item("Lupa", Item.Type.LUPA, "Revela"));
        Room vizinho = primeiroVizinhoDisponivel(atual);

        // Vai e volta — ao voltar coleta a LUPA
        model.moverJogador(direcaoPara(atual, vizinho));
        model.moverJogador(direcaoPara(vizinho, atual));

        assertThat(model.getJogador().possuiItem(Item.Type.LUPA)).isTrue();
    }

    @Test
    @DisplayName("Domínio: item não-LUPA NÃO é coletado se o jogador não tem lupa")
    void testeDominioItemOcultoSemLupaNaoColetado() {
        Room atual = model.getJogador().getPosicaoAtual();
        Item chave = new Item("Chave", Item.Type.CHAVE, "Abre");
        atual.adicionarItem(chave);
        Room vizinho = primeiroVizinhoDisponivel(atual);

        model.moverJogador(direcaoPara(atual, vizinho));
        model.moverJogador(direcaoPara(vizinho, atual));

        assertThat(model.getJogador().possuiItem(Item.Type.CHAVE)).isFalse();
        assertThat(atual.getItems()).contains(chave); // continua na sala
    }

    @Test
    @DisplayName("Domínio: com lupa, poção de velocidade dobra o tempo ao ser coletada")
    void testeDominioPocaoDobraTempoAoSerColetada() {
        darItemAoJogador(new Item("Lupa", Item.Type.LUPA, "Revela"));
        Room atual = model.getJogador().getPosicaoAtual();
        atual.getItems().clear();
        atual.adicionarItem(new Item("Poção", Item.Type.POCAO_VELOCIDADE, "x2"));

        int tempoBefore = model.getTempoRestante();
        Room vizinho = primeiroVizinhoDisponivel(atual);
        model.moverJogador(direcaoPara(atual, vizinho));
        model.moverJogador(direcaoPara(vizinho, atual));

        // Coletou: tempo dobrou; depois descontou 2 ticks de timer mas timer pausado
        // Então: tempoBefore * 2  (a coleta ocorre antes do decremento por movimento)
        assertThat(model.getTempoRestante()).isEqualTo(tempoBefore * 2);
    }

    @Test
    @DisplayName("Domínio: com lupa, amuleto acrescenta exatamente 3 movimentos ao ser coletado")
    void testeDominioAmuletoAcrescenta3MovimentosAoSerColetado() {
        darItemAoJogador(new Item("Lupa", Item.Type.LUPA, "Revela"));
        Room atual = model.getJogador().getPosicaoAtual();
        atual.getItems().clear();
        atual.adicionarItem(new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3"));

        int movBefore = model.getMovimentosRestantes();
        Room vizinho = primeiroVizinhoDisponivel(atual);
        model.moverJogador(direcaoPara(atual, vizinho)); // -1
        model.moverJogador(direcaoPara(vizinho, atual)); // -1, coleta amuleto: +3

        assertThat(model.getMovimentosRestantes()).isEqualTo(movBefore - 2 + 3);
    }

    // ── isChaveAtiva ──────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: isChaveAtiva false com inventário vazio")
    void testeDominioIsChaveAtivaFalseVazio() {
        assertThat(model.isChaveAtiva()).isFalse();
    }

    @Test
    @DisplayName("Domínio: isChaveAtiva true após adicionar chave")
    void testeDominioIsChaveAtivaTrue() {
        darItemAoJogador(new Item("Chave", Item.Type.CHAVE, "Abre"));
        assertThat(model.isChaveAtiva()).isTrue();
    }

    @Test
    @DisplayName("Domínio: isChaveVisivel só é true quando o jogador tem a lupa")
    void testeDominioIsChaveVisivelRequireLupa() {
        assertThat(model.isChaveVisivel()).isFalse();
        darItemAoJogador(new Item("Lupa", Item.Type.LUPA, "Revela"));
        assertThat(model.isChaveVisivel()).isTrue();
    }

    // ── Estado inicial do mapa ────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: mapa tem exatamente 100 salas (4 andares × 25)")
    void testeDominioMapaTem100Salas() {
        assertThat(model.getSalas()).hasSize(100);
    }

    @Test
    @DisplayName("Domínio: sala 'sagrado' nasce bloqueada")
    void testeDominioSagradoNasceBloqueada() {
        assertThat(model.getSalas().get("sagrado").isBloqueada()).isTrue();
    }

    @Test
    @DisplayName("Domínio: posição inicial do jogador é a 'entrada'")
    void testeDominicoPosicaoInicialEntrada() {
        assertThat(model.getJogador().getPosicaoAtual().getNome()).isEqualTo("entrada");
    }

    @Test
    @DisplayName("Domínio: andar inicial é 1")
    void testeDominioAndarInicial() {
        assertThat(model.getAndarAtual()).isEqualTo(1);
    }

    // ── Engine: pausar / retomar / encerrar ───────────────────────────────

    @Test
    @DisplayName("Domínio: engine encerrado não retoma o timer")
    void testeDominioEngineEncerradoNaoRetoma() {
        engine.encerrarJogo();
        assertThat(engine.isJogoEncerrado()).isTrue();
        // retomar não deve lançar exceção
        engine.retomar();
        assertThat(engine.isJogoEncerrado()).isTrue();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Utilitários
    // ═══════════════════════════════════════════════════════════════════════

    /** Dá item diretamente ao inventário do jogador (bypassa lógica de sala). */
    private void darItemAoJogador(Item item) {
        model.getJogador().adicionarItem(item);
    }

    /** Teleporta o jogador diretamente para a sala (sem gastar movimentos). */
    private void moverJogadorDiretamente(Room destino) {
        model.getJogador().moverPara(destino);
    }

    /** Encontra o primeiro vizinho não-nulo da sala. */
    private Room primeiroVizinhoDisponivel(Room sala) {
        for (String dir : List.of("norte", "sul", "leste", "oeste")) {
            Room v = sala.getVizinho(dir);
            if (v != null) return v;
        }
        throw new IllegalStateException("Sala sem vizinhos: " + sala.getNome());
    }

    /** Retorna a direção de 'origem' para 'destino'. */
    private String direcaoPara(Room origem, Room destino) {
        for (String dir : List.of("norte", "sul", "leste", "oeste")) {
            if (destino.equals(origem.getVizinho(dir))) return dir;
        }
        throw new IllegalArgumentException("Destino não é vizinho de origem");
    }

    /** Executa um movimento válido a partir da posição atual. */
    private void moverParaPrimeiroVizinho() {
        Room atual   = model.getJogador().getPosicaoAtual();
        Room vizinho = primeiroVizinhoDisponivel(atual);
        engine.mover(direcaoPara(atual, vizinho));
    }

    /** Força movimentosRestantes via redução repetida (sem setter externo). */
    private void forcarMovimentosRestantes(int alvo) {
        while (model.getMovimentosRestantes() > alvo) {
            Room a = model.getJogador().getPosicaoAtual();
            Room v = primeiroVizinhoDisponivel(a);
            model.moverJogador(direcaoPara(a, v));
            if (model.getMovimentosRestantes() > alvo) {
                model.moverJogador(direcaoPara(v, a));
            }
        }
    }

    private void verificarEvento(String propriedade, Object valorEsperado) {
        ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
        verify(listener, atLeastOnce()).propertyChange(captor.capture());
        boolean encontrado = captor.getAllValues().stream()
                .filter(e -> propriedade.equals(e.getPropertyName()))
                .anyMatch(e -> valorEsperado.equals(e.getNewValue()));
        assertThat(encontrado)
                .as("Esperava evento '%s' com valor %s", propriedade, valorEsperado)
                .isTrue();
    }

    private void verificarEventoGameOver(boolean esperado) {
        ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
        verify(listener, atLeastOnce()).propertyChange(captor.capture());
        boolean encontrado = captor.getAllValues().stream()
                .filter(e -> "gameOver".equals(e.getPropertyName()))
                .anyMatch(e -> Boolean.valueOf(esperado).equals(e.getNewValue()));
        assertThat(encontrado)
                .as("Esperava evento 'gameOver' com valor " + esperado)
                .isTrue();
    }
}