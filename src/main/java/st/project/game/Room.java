package st.project.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Room {
    private final String nome;
    private final int x;
    private final int y;               // coordenadas no grid
    private final List<Item> items;       // itens presentes na sala
    private boolean bloqueada;      // se precisa de chave para entrar
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
            if (i.getTipo() == tipo) return true;}
        return false;
    }

    public Item getItemPorTipo(Item.Type tipo) {
        for (Item i : items) {
            if (i.getTipo() == tipo) return i;
        }
        return null;
    }
    // Na classe Room


    public void setVizinho(String direcao, Room vizinho) {
        vizinhos.put(direcao, vizinho);
    }

    public Room getVizinho(String direcao) {
        return vizinhos.get(direcao);
    }


}