package st.project.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Player {
    private Room posicaoAtual;
    private final List<Item> inventario;
    private final Stack<Room> historico;   // para visualizar o trajeto

    public Player(Room inicio) {
        this.posicaoAtual = inicio;
        this.inventario = new ArrayList<>();
        this.historico = new Stack<>();
        this.historico.push(inicio);
    }

    public Room getPosicaoAtual() { return posicaoAtual; }
    public List<Item> getInventario() { return inventario; }
    public Stack<Room> getHistorico() { return historico; }

    public boolean moverPara(Room destino) {
        if (destino == null) return false;
        if (destino.isBloqueada()) {
            // verifica se possui chave
            boolean temChave = inventario.stream().anyMatch(i -> i.getTipo() == Item.Type.CHAVE);
            if (!temChave) return false;
        }
        this.posicaoAtual = destino;
        historico.push(destino);
        return true;
    }

    public void adicionarItem(Item item) {
        if (item == null) return;
        inventario.add(item);
    }

    public Item removerItem(Item item) {
        if (inventario.remove(item)) return item;
        return null;
    }

    public boolean possuiItem(Item.Type tipo) {
        return inventario.stream().anyMatch(i -> i.getTipo() == tipo);
    }

    public void usarItem(Item item) {
        // efeitos específicos são tratados pelo GameEngine
        // aqui apenas removemos se for de uso único
        if (item.getTipo() == Item.Type.POCAO_VELOCIDADE ||
                item.getTipo() == Item.Type.AMULETO_VISAO) {
            inventario.remove(item);
        }
    }
}