package com.playlist.composite;


import com.playlist.core.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composite do padrão Composite: uma playlist que pode conter faixas e outras playlists.
 */
public class PlaylistNode implements MediaItem {

    // Nome da playlist:
    private final String name;

    // Lista que armazena os filhos diretos desta playlist:
    private final List<MediaItem> children;

    /**
     * Cria uma playlist vazia.
     *
     * @param name nome da playlist. Não pode ser nulo nem em branco.
     * @throws IllegalArgumentException se o nome for nulo ou em branco.
     */
    public PlaylistNode(String name) {
        // Verifica se o nome é nulo ou contém apenas espaços.
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("O nome da playlist não pode ser nulo ou em branco.");
        }

        // Guarda on nome no construtor
        this.name = name;

        // Cria a lista inicialmente vazia
        this.children = new ArrayList<>();
    }

    /**
     * Adiciona um item ao final da playlist.
     *
     * @param item item a ser adicionado.
     * @return a própria playlist, permitindo encadear chamadas.
     * @throws IllegalArgumentException se o item for nulo, for a própria playlist ou contiver a própria playlist (o que criaria um ciclo).
     */
    public PlaylistNode add(MediaItem item) {
        // se o item for nulo:
        if (item == null) {
            throw new IllegalArgumentException("O item não pode ser nulo.");
        }

        // Uma playlist não pode ser adicionada a si mesma.
        if (item == this) {
            throw new IllegalArgumentException("Uma playlist não pode conter a si mesma.");
        }

        // Se o item for uma playlist que já contém esta playlist:
        if (item instanceof PlaylistNode playlist && playlist.contains(this)) {
            throw new IllegalArgumentException("A adição criaria um ciclo.");
        }

        // adiciona no final da lista:
        children.add(item);

        // Retorna a própria playlist para permitir encadeamento.
        return this;
    }

    /**
     * Remove um filho direto da playlist.
     *
     * @param item item a ser removido.
     * @return {@code true} se o item era filho direto e foi removido.
     */
    public boolean remove(MediaItem item) {
        // só um method para remover o item da lista tranquilo
        return children.remove(item);
    }

    /**
     * Lista os filhos diretos da playlist.
     *
     * @return uma lista imutável com os filhos, na ordem de inserção.
     */
    public List<MediaItem> getChildren() {
        // retorna apenas uma visualização da lista
        return Collections.unmodifiableList(children);
    }

    /**
     * Verifica se o item está em qualquer nível abaixo desta playlist.
     *
     * @param item item procurado.
     * @return {@code true} se o item for filho direto ou descendente.
     */
    public boolean contains(MediaItem item) {
        // Percorrer os filhos:
        for (MediaItem child : children) {
            // Se encontramos exatamente o objeto procurado, retornamos true:
            if (child == item) {
                return true;
            }

            // Se o filho também for uma playlist, procuramos recursivamente dentro dela:
            if (child instanceof PlaylistNode playlist && playlist.contains(item)) {
                return true;
            }
        }

        // O item não foi encontrado nesta playlist nem em seus descendentes
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getDurationSeconds() {
        // acumulação total em segundos:
        int total = 0;

        for (MediaItem child : children) {
            total += child.getDurationSeconds();
        }

        return total;
    }

    @Override
    public int getTrackCount() {
        // Acumulador total de faixas:
        int total = 0;

        // fazemos o mesmo aqui:
        for (MediaItem child : children) {
            total += child.getTrackCount(); // -> só mudamos o metodo aqui para pegar as faixas e não a duração em segundos
        }

        return total;
    }

    @Override
    public List<Track> flatten() {
        // aqui não vai ser acumulador e sim uma lista que receberá todas as faixas encontradas:
        List<Track> tracks = new ArrayList<>();

        // percorremos de novo o children:
        for (MediaItem child : children) {
            tracks.addAll(child.flatten()); // Adiciona todas as faixas daquele filho. Se for TrackItem, haverá uma faixa. Se for PlaylistNode, haverá uma busca recursiva.
        }

        return tracks;
    }
}
