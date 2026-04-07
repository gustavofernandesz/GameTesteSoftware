package st.project;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import st.project.game.GameEngine;
import st.project.game.Item;
import st.project.game.Room;

import javax.swing.Timer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameEngineEstruturaTest {

    @Mock
    private GameEngine.TimerListener listenerMock;

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine(listenerMock);
    }

    @Test
    @DisplayName("Teste de Estrutura: Inicialização deve criar mapa com 25 salas")
    void testeEstruturaInicializacaoMapa25Salas() {
        Map<String, Room> salas = engine.getSalas();
        assertEquals(25, salas.size());
    }

    @Test
    @DisplayName("Teste de Estrutura/ Domínio: Sala sagrado deve iniciar bloqueada")
    void testeEstruturaInicializacaoSalaSagradoBloqueada() {
        Room sagrado = engine.getSalas().get("sagrado");
        assertTrue(sagrado.isBloqueada());
    }

    @Test
    @DisplayName("Teste de Estrutura/ Domínio: Adjacências iniciais do mapa devem estar corretas")
    void testeEstruturaInicializacaoAdjacenciasCorretas() {
        // Verifica itens nas salas específicas
        assertTrue(engine.getSalas().get("biblioteca").getItems().stream()
                .anyMatch(i -> i.getTipo() == Item.Type.CHAVE));
        assertTrue(engine.getSalas().get("cozinha").getItems().stream()
                .anyMatch(i -> i.getTipo() == Item.Type.POCAO_VELOCIDADE));
        assertTrue(engine.getSalas().get("jardim").getItems().stream()
                .anyMatch(i -> i.getTipo() == Item.Type.AMULETO_VISAO));
        assertTrue(engine.getSalas().get("sagrado").getItems().stream()
                .anyMatch(i -> i.getTipo() == Item.Type.CALICE));
    }

    @Test
    @DisplayName("Teste de Estrutura: Adjacências devem estar corretas no grid")
    void inicializacao_AdjacenciasDevemEstarCorretas() {
        // Testa algumas adjacências esperadas no grid 5x5
        Room entrada = engine.getSalas().get("entrada");
        // Entrada está na posição (0,0) -> só vizinhos leste e sul
        assertNotNull(entrada.getVizinho("leste"));
        assertNotNull(entrada.getVizinho("sul"));
        assertNull(entrada.getVizinho("norte"));
        assertNull(entrada.getVizinho("oeste"));
    }

    @Test
    @DisplayName("Estrutura: sala na posição (1,1) deve ter vizinhos norte e oeste")
    void testeEstruturaVizinhosNorteOesteExistem() {
        // Encontra uma sala que não esteja na borda (linha > 0 e coluna > 0)
        Room salaComNorteOeste = engine.getSalas().values().stream()
                .filter(r -> r.getX() > 0 && r.getY() > 0)
                .findFirst()
                .orElseThrow();

        assertNotNull(salaComNorteOeste.getVizinho("norte"));
        assertNotNull(salaComNorteOeste.getVizinho("oeste"));
    }

    @Test
    @DisplayName("Estrutura/Fronteira: sala na borda norte (y=0) não deve ter vizinho norte")
    void testeEstruturaBordaNorteNaoTemVizinho() {
        engine.getSalas().values().stream()
                .filter(r -> r.getY() == 0)
                .forEach(r -> assertNull(r.getVizinho("norte")));
    }

    @Test
    @DisplayName("Estrutura/Fronteira: sala na borda oeste (x=0) não deve ter vizinho oeste")
    void testeEstruturaBordaOesteNaoTemVizinho() {
        engine.getSalas().values().stream()
                .filter(r -> r.getX() == 0)
                .forEach(r -> assertNull(r.getVizinho("oeste")));
    }

    @Test
    @DisplayName("Estrutura/Fronteira: sala na borda leste (x=4) não deve ter vizinho leste")
    void testeEstruturaBordaLesteNaoTemVizinho() {
        engine.getSalas().values().stream()
                .filter(r -> r.getX() == 4)
                .forEach(r -> assertNull(r.getVizinho("leste")));
    }

    @Test
    @DisplayName("Eatrutura/Fronteira: sala na borda sul (y=4) não deve ter vizinho sul")
    void testeEstruturaBordaSulNaoTemVizinho() {
        engine.getSalas().values().stream()
                .filter(r -> r.getY() == 4)
                .forEach(r -> assertNull(r.getVizinho("sul")));
    }

    @Test
    @DisplayName("Estrutura: sala interna deve ter vizinho leste")
    void testeEstruturaVizinhoLesteExiste() {
        Room salaComLeste = engine.getSalas().values().stream()
                .filter(r -> r.getX() < 4)
                .findFirst()
                .orElseThrow();
        assertNotNull(salaComLeste.getVizinho("leste"));
    }

    @Test
    @DisplayName("Teste de Estrutura/Domínio: Listener deve ser notificado ao movimentar")
    void testeEstruturaListenerNotificaMovimento() {
        engine.moverJogador("leste");
        verify(listenerMock).onMovimentoRealizado(6);
    }

    @Test
    @DisplayName("Teste de Estrutura: Listener deve ser notificado a cada tick do timer")
    void testeEstruturaListenerNotificaTempoAoDispararTimer() throws Exception {
        Timer timer = getTimerFromEngine(engine);
        // Simula um tick do timer
        fireTimerAction(timer);
        verify(listenerMock).onTempoAtualizado(59);
    }

    @Test
    @DisplayName("Estrutura/Fronteira: timer deve parar após tempo zerar")
    void testeEstruturaTimerParaAposTempoZerar() throws Exception {
        Timer timer = getTimerFromEngine(engine);
        assertTrue(timer.isRunning());

        for (int i = 0; i < 60; i++) {
            fireTimerAction(timer);
        }

        assertFalse(timer.isRunning());
    }

    @Test
    @DisplayName("Estrutura: timer deve estar rodando ao iniciar o engine")
    void testeEstruturaTimerRodandoAoIniciar() throws Exception {
        Timer timer = getTimerFromEngine(engine);
        assertTrue(timer.isRunning());
    }


    @Test
    @DisplayName("Estrutura/fronteira: timer deve parar ao encerrar jogo por movimentos")
    void testeEstruturaTimerParaAoEncerrarPorMovimentos() throws Exception {
        Timer timer = getTimerFromEngine(engine);
        assertTrue(timer.isRunning());

        engine.setMovimentosRestantes(0);
        engine.moverJogador("leste");

        assertFalse(timer.isRunning());
    }

    @Test
    @DisplayName("Estrutura/Domínio: timer deve parar ao encerrar jogo por vitória")
    void testeEstruturaTimerParaAoVencer() throws Exception {
        Timer timer = getTimerFromEngine(engine);

        Room sagrado = engine.getSalas().get("sagrado");
        Room vizinha = obterVizinhaDe(sagrado);
        engine.getJogador().moverPara(vizinha);
        engine.getJogador().adicionarItem(new Item("Chave", Item.Type.CHAVE, ""));
        engine.moverJogador(obterDirecaoPara(vizinha, sagrado));

        assertFalse(timer.isRunning());
    }

    @Test
    @DisplayName("Estrutural/Fronteira: isChaveAtiva retorna false quando inventário está vazio")
    void testeFronteiraIsChaveAtivaInventarioVazio() {
        assertFalse(engine.isChaveAtiva());
    }

    @Test
    @DisplayName("Estrutural/Fronteira: isChaveAtiva retorna false com outros itens mas sem chave")
    void testeFronteiraIsChaveAtivaComOutrosItens() {
        engine.getJogador().adicionarItem(
                new Item("Amuleto", Item.Type.AMULETO_VISAO, ""));
        assertFalse(engine.isChaveAtiva());
    }

    @Test
    @DisplayName("Estrutural/Fronteira: isChaveAtiva retorna true após adicionar chave")
    void testeFronteiraIsChaveAtivaComChave() {
        engine.getJogador().adicionarItem(
                new Item("Chave", Item.Type.CHAVE, ""));
        assertTrue(engine.isChaveAtiva());
    }

    @Test
    @DisplayName("Teste de Estrutura: Listener deve ser notificado ao esgotar o tempo")
    void testeEstruturaListenerNotificaFimDeJogoQuandoTempoZera() throws Exception {
        Timer timer = getTimerFromEngine(engine);
        // Dispara 60 vezes
        for (int i = 0; i < 60; i++) {
            fireTimerAction(timer);
        }
        verify(listenerMock).onJogoTerminado(false);
    }

    @Test
    @DisplayName("Teste de Estrutura: Após jogo encerrado, movimentos devem ser ignorados")
    void testeEstruturaAposEncerrarJogoMovimentosIgnorados() {
        engine.setJogoAtivo(false);
        boolean moveu = engine.moverJogador("leste");
        assertFalse(moveu);
        verify(listenerMock, never()).onMovimentoRealizado(anyInt());
    }

    @Test
    @DisplayName("Teste de Estrutura: Ao encerrar jogo, timer deve parar de atualizar")
    void testeEstruturaAoEncerrarJogoTimerPara() throws Exception {
        Timer timer = getTimerFromEngine(engine);
        // Encerra o jogo
        engine.setJogoAtivo(false);
        // Simula um tick do timer (não deve afetar)
        fireTimerAction(timer);
        // Como jogo não está ativo, tempo não deve diminuir
        assertEquals(60, engine.getTempoRestante());
        verify(listenerMock, never()).onTempoAtualizado(anyInt());
    }

    @Test
    @DisplayName("Teste de Estrutura: Chave não deve ser consumida ao entrar no sagrado")
    void testeEstruturaChaveNaoConsumidaAoEntrarNoSagrado() {
        // Dá a chave ao jogador
        engine.getJogador().adicionarItem(new Item("Chave", Item.Type.CHAVE, ""));
        Room sagrado = engine.getSalas().get("sagrado");
        Room vizinha = obterVizinhaDe(sagrado);
        engine.getJogador().moverPara(vizinha);
        String direcao = obterDirecaoPara(vizinha, sagrado);

        engine.moverJogador(direcao);

        // A chave ainda deve estar no inventário
        assertTrue(engine.getJogador().possuiItem(Item.Type.CHAVE));
    }

    @Test
    @DisplayName("Estrutura: seed fixa deve produzir mapa determinístico")
    void testeEstruturaSeedFixaMapaDeterministico() {
        // Dois engines com mesma seed devem ter mesma ordem de salas
        GameEngine engine1 = new GameEngine(null, new Random(42));
        GameEngine engine2 = new GameEngine(null, new Random(42));

        // Compara posições das salas intermediárias
        engine1.getSalas().forEach((nome, sala) -> {
            Room salaEngine2 = engine2.getSalas().get(nome);
            assertEquals(sala.getX(), salaEngine2.getX());
            assertEquals(sala.getY(), salaEngine2.getY());
        });
    }

    @Test
    @DisplayName("Estrutura: vizinho leste de entrada deve ser a sala na posicao (1,0)")
    void testeEstruturaVizinhoLesteEntradaEhPosicao1x0() {
        Room entrada = engine.getSalas().get("entrada"); // x=0, y=0
        Room vizinhoLeste = entrada.getVizinho("leste");

        assertNotNull(vizinhoLeste);
        assertEquals(1, vizinhoLeste.getX(), "Vizinho leste de (0,0) deve ter x=1");
        assertEquals(0, vizinhoLeste.getY(), "Vizinho leste de (0,0) deve ter y=0");
    }

    @Test
    @DisplayName("Estrutura: vizinho leste de sala (0,1) deve ter x=1 e y=1")
    void testeEstruturaVizinhoLesteLinha1Coluna0() {
        // Procura a sala em x=0, y=1 (segunda linha, primeira coluna)
        Room sala01 = engine.getSalas().values().stream()
                .filter(r -> r.getX() == 0 && r.getY() == 1)
                .findFirst().orElseThrow();
        Room vizinhoLeste = sala01.getVizinho("leste");

        assertNotNull(vizinhoLeste);
        assertEquals(1, vizinhoLeste.getX());
        assertEquals(1, vizinhoLeste.getY(), // x=1,y=1 — se fosse i/5 daria y errado
                "Vizinho leste deve estar na mesma linha");
    }

    @Test
    @DisplayName("Estrutura: construtor com seed deve inicializar itens corretamente")
    void testeEstruturaConstrutorComSeedInicializaItens() {
        GameEngine engineComSeed = new GameEngine(listenerMock, new Random(42));

        assertTrue(engineComSeed.getSalas().get("biblioteca").getItems().stream()
                        .anyMatch(i -> i.getTipo() == Item.Type.CHAVE),
                "biblioteca deve ter chave mesmo com seed");
        assertTrue(engineComSeed.getSalas().get("cozinha").getItems().stream()
                        .anyMatch(i -> i.getTipo() == Item.Type.POCAO_VELOCIDADE),
                "cozinha deve ter pocao mesmo com seed");
        assertTrue(engineComSeed.getSalas().get("jardim").getItems().stream()
                        .anyMatch(i -> i.getTipo() == Item.Type.AMULETO_VISAO),
                "jardim deve ter amuleto mesmo com seed");
        assertTrue(engineComSeed.getSalas().get("sagrado").getItems().stream()
                        .anyMatch(i -> i.getTipo() == Item.Type.CALICE),
                "sagrado deve ter calice mesmo com seed");
    }

    @Test
    @DisplayName("Estrutura: construtor com seed deve iniciar timer e decrementar tempo")
    void testeEstruturaConstrutorComSeedIniciaTimer() throws Exception {
        GameEngine engineComSeed = new GameEngine(listenerMock, new Random(42));

        Timer timer = getTimerFromEngine(engineComSeed);
        assertTrue(timer.isRunning(), "Timer deve estar rodando no construtor com seed");

        int tempoAntes = engineComSeed.getTempoRestante();
        fireTimerAction(timer);
        assertEquals(tempoAntes - 1, engineComSeed.getTempoRestante(),
                "Timer com seed deve decrementar tempo normalmente");
    }

    @Test
    @DisplayName("Estrutura: seeds diferentes devem produzir mapas diferentes")
    void testeEstruturaSeedsDiferentesMapasDiferentes() {
        GameEngine engine1 = new GameEngine(null, new Random(1));
        GameEngine engine2 = new GameEngine(null, new Random(999));

        // Pelo menos uma sala intermediária deve estar em posição diferente
        boolean algumaDiferente = engine1.getSalas().entrySet().stream()
                .filter(e -> !e.getKey().equals("entrada") && !e.getKey().equals("sagrado"))
                .anyMatch(e -> {
                    Room r2 = engine2.getSalas().get(e.getKey());
                    return e.getValue().getX() != r2.getX() || e.getValue().getY() != r2.getY();
                });
        assertTrue(algumaDiferente);
    }

    @Test
    @DisplayName("Estrutura: vizinho oeste de sala (1,0) deve ser entrada em (0,0)")
    void testeEstruturaVizinhoOesteLinha0Coluna1EhPosicao0x0() {
        // Sala em x=1, y=0 — vizinho oeste deve ser x=0, y=0 (entrada)
        Room sala10 = engine.getSalas().values().stream()
                .filter(r -> r.getX() == 1 && r.getY() == 0)
                .findFirst().orElseThrow();

        Room vizinhoOeste = sala10.getVizinho("oeste");

        assertNotNull(vizinhoOeste);
        assertEquals(0, vizinhoOeste.getX(), "Vizinho oeste de (1,0) deve ter x=0");
        assertEquals(0, vizinhoOeste.getY(), "Vizinho oeste deve estar na mesma linha y=0");
    }

    @Test
    @DisplayName("Estrutura: vizinho oeste de sala (1,1) deve ter x=0 e y=1")
    void testeEstruturaVizinhoOesteLinha1Coluna1EhPosicao0x1() {
        // Sala em x=1, y=1 — com i/5 em vez de i*5, o índice seria calculado errado
        Room sala11 = engine.getSalas().values().stream()
                .filter(r -> r.getX() == 1 && r.getY() == 1)
                .findFirst().orElseThrow();

        Room vizinhoOeste = sala11.getVizinho("oeste");

        assertNotNull(vizinhoOeste);
        assertEquals(0, vizinhoOeste.getX(), "Vizinho oeste de (1,1) deve ter x=0");
        assertEquals(1, vizinhoOeste.getY(), "Vizinho oeste deve estar na mesma linha y=1");
    }

    // Métodos auxiliares para navegação
    private Room obterVizinhaDe(Room room) {
        for (String dir : new String[]{"norte", "sul", "leste", "oeste"}) {
            Room viz = room.getVizinho(dir);
            if (viz != null) return viz;
        }
        throw new IllegalStateException("Sala não tem vizinhos?");
    }

    private String obterDirecaoPara(Room origem, Room destino) {
        for (String dir : new String[]{"norte", "sul", "leste", "oeste"}) {
            if (origem.getVizinho(dir) == destino) return dir;
        }
        throw new IllegalArgumentException("Destino não é vizinho");
    }

    // --- Utilitários para manipular o Timer interno ---
    private Timer getTimerFromEngine(GameEngine engine) throws Exception {
        Field field = GameEngine.class.getDeclaredField("timer");
        field.setAccessible(true);
        return (Timer) field.get(engine);
    }

    private void fireTimerAction(Timer timer) throws Exception {
        Class<?> timerClass = timer.getClass();
        Method getListeners = timerClass.getDeclaredMethod("getListeners", Class.class);
        getListeners.setAccessible(true);
        Object[] listeners = (Object[]) getListeners.invoke(timer, Class.forName("java.awt.event.ActionListener"));
        if (listeners.length > 0) {
            java.awt.event.ActionListener al = (java.awt.event.ActionListener) listeners[0];
            al.actionPerformed(new java.awt.event.ActionEvent(timer, 0, null));
        }
    }
}