package st.project.game.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class Room implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String nome;
    private final int x;
    private final int y;
    private final List<Item> items;
    private boolean bloqueada;
    private final HashMap<String, Room> vizinhos;

    public Room(String nome, int x, int y) {
        this.nome = nome;
        this.x = x;
        this.y = y;
        this.items = new ArrayList<>();
        this.bloqueada = false;
        vizinhos = new HashMap<>();
    }

    public String getNome() { return nome; }
    public int getX() { return x; }
    public int getY() { return y; }
    public List<Item> getItems() { return items; }
    public boolean isBloqueada() { return bloqueada; }
    public void setBloqueada(boolean bloqueada) { this.bloqueada = bloqueada; }

    public void adicionarItem(Item item) {
        if (item != null) {
            items.add(item);
        }
    }

    public Item removerItem(Item item) {
        if (items.remove(item)) return item;
        return null;
    }

    public boolean contemItem(Item.Type tipo) {
        for (Item i : items) {
            if (i.getTipo() == tipo) return true;
        }
        return false;
    }

    public Item getItemPorTipo(Item.Type tipo) {
        for (Item i : items) {
            if (i.getTipo() == tipo) return i;
        }
        return null;
    }

    public void setVizinho(String direcao, Room vizinho) {
        vizinhos.put(direcao, vizinho);
    }

    public Room getVizinho(String direcao) {
        return vizinhos.get(direcao);
    }
}