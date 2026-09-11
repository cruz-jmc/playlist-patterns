package com.playlist.composite;

import com.playlist.core.Track;

import java.util.List;

/**
 * Folha do padrão Composite: envolve uma única {@link Track}.
 */
public class TrackItem implements MediaItem {

    // Guarda a Track original que foi recebida pelo TrackItem.
    private final Track track;

    /**
     * Cria a folha a partir de uma faixa.
     *
     * @param track faixa envolvida. Não pode ser nula.
     * @throws IllegalArgumentException se {@code track} for nula.
     */
    public TrackItem(Track track) {
        // verifica se a faixa recebida é nula:
        if (track == null) {
            throw new IllegalArgumentException("A faixa não pode ser nula.");
        }

        // construtor de fato:
        this.track = track;
    }

    /**
     * Devolve a faixa envolvida por esta folha.
     *
     * @return a faixa original.
     */
    public Track getTrack() {

      return track;
    }

    @Override
    public String getName() {
      return track.title();
    }

    @Override
    public int getDurationSeconds() {
        return track.durationSeconds();
    }

    @Override
    public int getTrackCount() {
        return 1; // -> porque cada TrackItem representa apenas 1 faixa.
    }

    @Override
    public List<Track> flatten() {
      // Retorna uma lista contendo somente a Track encapsulada:
      return List.of(track);
    }
}
