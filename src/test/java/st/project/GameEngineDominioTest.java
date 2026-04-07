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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameEngineDominioTest {

    @Mock
    private GameEngine.TimerListener listenerMock;

    private GameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GameEngine(listenerMock);
    }

    @Test
    @DisplayName("Teste de Domínio: Mover para direção válida deve mover jogador e decrementar movimentos")
    void testeDominioMoverJogadorDirecaoValida() {
        // Arrange: a partir da entrada, o vizinho leste deve existir
        Room entrada = engine.getSalas().get("entrada");
        Room vizinhoLeste = entrada.getVizinho("leste");
        assertNotNull(vizinhoLeste, "Pré-condição: deve haver vizinho leste");

        // Act
        boolean moveu = engine.moverJogador("leste");

        // Assert
        assertTrue(moveu);
        assertEquals(vizinhoLeste, engine.getJogador().getPosicaoAtual());
        verify(listenerMock).onMovimentoRealizado(6); // 7 - 1
    }

    @Test
    @DisplayName("Teste de Domínio: Mover para direção inválida deve retornar false e não alterar posição")
    void testeDominioMoverJogadorDirecaoInvalida() {
        // Arrange: direção sem vizinho (norte da entrada pode não existir no grid)
        Room entrada = engine.getSalas().get("entrada");
        if (entrada.getVizinho("norte") != null) {
            // Se existir, usa uma direção que não existe (ex: "lixao")
            assertFalse(engine.moverJogador("lixao"));
        } else {
            assertFalse(engine.moverJogador("norte"));
        }
        // Posição não deve ter mudado
        assertEquals(entrada, engine.getJogador().getPosicaoAtual());
    }

    @Test
    @DisplayName("Teste de Domínio: Mover para sala com itens deve coletá-los e aplicar efeitos")
    void testeDominioMoverJogadorColetaItensDaSala() {
        // Arrange: obter a sala de destino (vizinho leste da entrada)
        Room entrada = engine.getSalas().get("entrada");
        Room destino = entrada.getVizinho("leste");
        assertNotNull(destino, "Pré-condição: deve existir vizinho leste");

        Item pocao = new Item("Poção", Item.Type.POCAO_VELOCIDADE, "Dobra tempo");
        destino.adicionarItem(pocao);
        int tempoAntes = engine.getTempoRestante();

        // Act: mover para a sala que contém a poção
        engine.moverJogador("leste");

        // Assert: item deve ter sido removido da sala e adicionado ao jogador
        assertFalse(destino.getItems().contains(pocao));
        assertTrue(engine.getJogador().getInventario().contains(pocao)); // ajuste conforme método real (getInventario() ou getItems())
        assertEquals(tempoAntes * 2, engine.getTempoRestante());
        verify(listenerMock).onTempoAtualizado(tempoAntes * 2);
    }

    @Test
    @DisplayName("Teste de Domínio: Entrar na sala sagrado com chave deve concluir a missão")
    void testeDominioMoverJogadorEntrarSagradoComChave() {
        // Arrange: forçar posição do jogador para a sala anterior ao sagrado
        // e dar a chave a ele
        Room salaChave = engine.getSalas().get("biblioteca");
        Item chave = salaChave.getItems().stream()
                .filter(i -> i.getTipo() == Item.Type.CHAVE)
                .findFirst()
                .orElseThrow();
        engine.getJogador().moverPara(salaChave);
        engine.coletarItensSala(); // coleta a chave

        Room sagrado = engine.getSalas().get("sagrado");
        Room vizinhaDoSagrado = obterVizinhaDe(sagrado);
        engine.getJogador().moverPara(vizinhaDoSagrado); // posiciona ao lado

        // Act
        String direcao = obterDirecaoPara(vizinhaDoSagrado, sagrado);
        boolean moveu = engine.moverJogador(direcao);

        // Assert
        assertTrue(moveu);
        assertTrue(engine.getMissao().isMissaoConcluida());
        assertFalse(engine.isJogoAtivo());
        verify(listenerMock).onJogoTerminado(true);
    }

    @Test
    @DisplayName("Teste de Domínio: Tentar entrar no sagrado sem chave deve falhar e manter posição")
    void testeDominioMoverJogadorTentarEntrarSagradoSemChave() {
        // Arrange: posicionar jogador ao lado do sagrado sem ter a chave
        Room sagrado = engine.getSalas().get("sagrado");
        Room vizinha = obterVizinhaDe(sagrado);
        engine.getJogador().moverPara(vizinha);
        assertFalse(engine.isChaveAtiva());

        // Act
        String direcao = obterDirecaoPara(vizinha, sagrado);
        boolean moveu = engine.moverJogador(direcao);

        // Assert
        assertFalse(moveu);
        assertEquals(vizinha, engine.getJogador().getPosicaoAtual());
        assertFalse(engine.getMissao().isMissaoConcluida());
        assertTrue(engine.isJogoAtivo());
    }

    @Test
    @DisplayName("Teste de Domínio: Esgotar movimentos ao mover deve encerrar jogo com derrota")
    void testeDominioMoverJogadorEsgotarMovimentos() {
        // Arrange: reduzir movimentos para 1
        engine.setMovimentosRestantes(1);

        // Act: mover para algum lugar
        engine.moverJogador("leste");

        // Assert: após o movimento, movimentos = 0 e jogo termina
        assertFalse(engine.isJogoAtivo());
        verify(listenerMock).onJogoTerminado(false);
    }

    @Test
    @DisplayName("Teste de Domínio: Coletar amuleto deve aumentar movimentos restantes")
    void testeDominioAplicarEfeitoAmuleto() {
        // Arrange: colocar amuleto na sala do jogador e coletar
        Room atual = engine.getJogador().getPosicaoAtual();
        Item amuleto = new Item("Amuleto", Item.Type.AMULETO_VISAO, "+3 mov");
        atual.adicionarItem(amuleto);
        int movAntes = engine.getMovimentosRestantes();

        // Act
        engine.coletarItensSala();

        // Assert
        assertEquals(movAntes + 3, engine.getMovimentosRestantes());
        verify(listenerMock).onMovimentoRealizado(movAntes + 3);
    }

    @Test
    @DisplayName("Teste de Domínio: Coletar chave não deve disparar eventos de tempo ou término")
    void testeDominioColetarItemChave() {
        Room atual = engine.getJogador().getPosicaoAtual();
        String direcao = "leste";
        Room destino = atual.getVizinho(direcao);
        if (destino == null) {
            direcao = "sul";
            destino = atual.getVizinho(direcao);
        }
        assertNotNull(destino);

        // Coloca a chave na sala de destino
        Item chave = new Item("Chave", Item.Type.CHAVE, "Abre");
        destino.adicionarItem(chave);

        reset(listenerMock);

        engine.moverJogador(direcao);

        assertFalse(destino.getItems().contains(chave));
        // Use getItems() ou getInventario() conforme a classe Player
        assertTrue(engine.getJogador().getInventario().contains(chave));

        verify(listenerMock, times(1)).onMovimentoRealizado(anyInt());
        verify(listenerMock, never()).onTempoAtualizado(anyInt());
        verify(listenerMock, never()).onJogoTerminado(anyBoolean());
    }

    @Test
    @DisplayName("Teste de Domínio: Coletar cálice após entrar no sagrado deve concluir missão sem efeitos adicionais")
    void testeDominioColetarItemCalice() {
        // Força a entrada na sala sagrado com chave
        Room sagrado = engine.getSalas().get("sagrado");
        Room vizinha = obterVizinhaDe(sagrado);
        engine.getJogador().moverPara(vizinha);
        engine.getJogador().adicionarItem(new Item("Chave", Item.Type.CHAVE, ""));

        // Reseta o mock para ignorar chamadas anteriores
        reset(listenerMock);

        String direcao = obterDirecaoPara(vizinha, sagrado);
        engine.moverJogador(direcao);

        // Missão concluída, jogo encerrado
        assertTrue(engine.getMissao().isMissaoConcluida());
        assertFalse(engine.isJogoAtivo());

        // O listener deve ter recebido apenas o evento de término (e movimento)
        verify(listenerMock, times(1)).onMovimentoRealizado(anyInt());
        verify(listenerMock, times(1)).onJogoTerminado(true);
        verify(listenerMock, never()).onTempoAtualizado(anyInt());
    }

// Se necessário, adicione os métodos auxiliares (já devem existir na classe)

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
}