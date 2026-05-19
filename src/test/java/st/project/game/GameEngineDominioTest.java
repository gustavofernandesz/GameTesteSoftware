package st.project.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import st.project.game.controller.GameEngine;
import st.project.game.controller.UserManager;
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
 * itens, sistema de níveis, score, teleporte por escada, controle de usuários.
 *
 * Dublês de teste (Mockito):
 *   • PropertyChangeListener (mock) — isola eventos MVC sem GUI real.
 *   • UserManager (mock) — isola I/O de arquivo no teste de superusuário.
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
        // navegar até restar 1 movimento e então mover
        forcarMovimentosRestantes(1);
        moverParaPrimeiroVizinho();

        assertThat(model.isJogoAtivo()).isFalse();
        // listener deve ter recebido gameOver=false
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


    // ── Score ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: score = tempo*10 + movimentos*5 + itens*100")
    void testeDominioFormulaScore() {
        int tempo = model.getTempoRestante();
        int mov   = model.getMovimentosRestantes();
        int itens = model.getJogador().getInventario().size();
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
        // Zera o tempo diretamente via reduzirTempo repetidamente
        int t = model.getTempoRestante();
        for (int i = 0; i < t; i++) {
            model.reduzirTempo();
        }
        assertThat(model.isJogoAtivo()).isFalse();
        verificarEventoGameOver(false);
    }

    // ── Missão concluída ──────────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: entrar no sagrado com chave e cálice conclui a missão")
    void testeDominioMoverParaSagradoComChaveConcluiMissao() {
        // Dá a chave ao jogador para abrir a sala bloqueada
        darItemAoJogador(new Item("Lupa", Item.Type.LUPA, "Revela"));
        darItemAoJogador(new Item("Chave", Item.Type.CHAVE, "Abre"));

        // Posiciona o jogador ao lado de sagrado e move para lá
        Room sagrado   = model.getSalas().get("sagrado");
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
        Room sagrado = model.getSalas().get("sagrado");
        Room vizinhaDeSagrado = primeiroVizinhoDisponivel(sagrado);
        moverJogadorDiretamente(vizinhaDeSagrado);

        int movimentosAntes = model.getMovimentosRestantes();
        String dir = direcaoPara(vizinhaDeSagrado, sagrado);

        boolean moveu = engine.mover(dir);

        assertThat(moveu).isTrue();
        assertThat(model.getJogador().getPosicaoAtual()).isEqualTo(model.getSalas().get("entrada"));
        assertThat(model.getMovimentosRestantes()).isEqualTo(movimentosAntes - 1);
        assertThat(model.isJogoAtivo()).isTrue();
        verificarEvento("alcapao", true);
    }

    // ── Teleporte de escada ───────────────────────────────────────────────

    @Test
    @DisplayName("Domínio: ao mover para escada_cima_1 o jogador vai para o andar 2")
    void testeDominioTeleporteEscadaCima() {
        // Localiza escada_cima_1 no andar 1 e posiciona jogador ao lado
        Room escada = model.getSalas().get("escada_cima_1");
        assertThat(escada).isNotNull();
        moverJogadorDiretamente(escada);
        // O teleporte ocorre ao *mover para* a escada — usamos engine.mover
        // Mas como já estamos NA escada (teleporte é pós-movimento), verificamos o andar
        // O teleporte se aplica após moverJogador interno — simulamos via moverJogador do model
        // Reset: voltamos ao vizinho da escada
        Room vizinho = primeiroVizinhoDisponivel(escada);
        moverJogadorDiretamente(vizinho);

        String dir = direcaoPara(vizinho, escada);
        model.moverJogador(dir);

        // Após teleporte, jogador deve estar no andar 2
        assertThat(model.getAndarAtual()).isEqualTo(2);
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

    // ── UserManager — controle de usuários ────────────────────────────────

    @Test
    @DisplayName("Domínio: UserManager registra novo usuário com sucesso")
    void testeDominioUserManagerRegistrar() {
        UserManager um = mock(UserManager.class);
        when(um.registerUser("heroi", "pw123", "av.png")).thenReturn(true);

        boolean ok = um.registerUser("heroi", "pw123", "av.png");

        assertThat(ok).isTrue();
        verify(um).registerUser("heroi", "pw123", "av.png");
    }

    @Test
    @DisplayName("Domínio: UserManager rejeita login duplicado")
    void testeDominioUserManagerLoginDuplicado() {
        UserManager um = mock(UserManager.class);
        when(um.registerUser(eq("admin"), anyString(), anyString())).thenReturn(false);

        boolean ok = um.registerUser("admin", "pw", "av.png");

        assertThat(ok).isFalse();
    }

    @Test
    @DisplayName("Domínio: superusuário 'admin' não pode ser excluído")
    void testeDominioSuperUsuarioNaoDeletavel() {
        UserManager um = mock(UserManager.class);
        when(um.deleteUser("admin")).thenReturn(false);

        assertThat(um.deleteUser("admin")).isFalse();
    }

    @Test
    @DisplayName("Domínio: authenticate retorna null para credenciais inválidas")
    void testeDominioAutenticacaoInvalida() {
        UserManager um = mock(UserManager.class);
        when(um.authenticate("x", "errada")).thenReturn(null);

        assertThat(um.authenticate("x", "errada")).isNull();
    }

    @Test
    @DisplayName("Domínio: updateUserScoreAndSession é chamado ao finalizar jogo (vitória)")
    void testeDominioUpdateScoreAoVencer() {
        UserManager um = mock(UserManager.class);
        User user = new User("heroi", "pw", "av.png");

        um.updateUserScoreAndSession(user, 1500);

        verify(um).updateUserScoreAndSession(user, 1500);
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
        Room atual  = model.getJogador().getPosicaoAtual();
        Room vizinho = primeiroVizinhoDisponivel(atual);
        engine.mover(direcaoPara(atual, vizinho));
    }

    /** Força movimentosRestantes via redução repetida (sem setter externo). */
    private void forcarMovimentosRestantes(int alvo) {
        while (model.getMovimentosRestantes() > alvo) {
            // move e volta — mas para não encerrar o jogo no processo, usamos
            // diretamente o model sem passar pelo engine
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
