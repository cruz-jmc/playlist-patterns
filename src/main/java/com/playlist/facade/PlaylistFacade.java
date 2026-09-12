package com.playlist.facade;

import com.playlist.adapter.TrackCatalog;
import com.playlist.composite.PlaylistNode;
import com.playlist.composite.TrackItem;
import com.playlist.core.Subscription;
import com.playlist.core.Track;
import com.playlist.core.TrackNotFoundException;
import com.playlist.decorator.AudioTrack;
import com.playlist.decorator.FadeInEffect;
import com.playlist.decorator.RawAudioTrack;
import com.playlist.decorator.VolumeEffect;
import com.playlist.proxy.ProtectedAudioStreamProxy;

import java.util.HashMap;
import java.util.Map;

/**
 * Fachada que esconde do mundo externo a colaboração entre catálogo, playlists,
 * streams protegidos e efeitos de áudio.
 * Quem usa a Playlist precisa conhecer apenas esta classe.
 */
public class PlaylistFacade {

    // Catálogo utilizado para localizar e listar as faixas
    private final TrackCatalog catalog;

    // Plano de assinatura do usuário
    private final Subscription plan;

    // Cache dos proxies, associado pelo ID da faixa
    private final Map<String, ProtectedAudioStreamProxy> streams;

    /**
     * Monta a fachada.
     *
     * @param catalog catálogo de faixas já adaptado.
     * @param plan    plano de assinatura de quem está usando o sistema.
     * @throws IllegalArgumentException se qualquer argumento for nulo.
     */
    public PlaylistFacade(TrackCatalog catalog, Subscription plan) {

        // Verifica se o catálogo ou o plano são nulos
        if (catalog == null || plan == null) {
            throw new IllegalArgumentException("catalog e plan não podem ser nulos");
        }

        // Construtor
        this.catalog = catalog;
        this.plan = plan;

        // Cria o mapa que armazenará os proxies reutilizáveis.
        this.streams = new HashMap<>();
    }

    /**
     * Monta uma playlist com todas as faixas do catálogo, na ordem em que o catálogo as devolve.
     *
     * @param name nome da playlist criada.
     * @return a playlist preenchida.
     */
    public PlaylistNode buildLibrary(String name) {

        // Cria uma playlist vazia com o nome recebido
        PlaylistNode library = new PlaylistNode(name);

        // Percorre todas as faixas válidas fornecidas pelo catálogo
        for (Track track : catalog.findAll()) {

            // Converte cada Track em um TrackItem e adiciona à playlist
            library.add(new TrackItem(track));
        }

        // Devolve a playlist completamente preenchida
        return library;
    }

    /**
     * Devolve os bytes de áudio de uma faixa, respeitando o plano de assinatura.
     *
     * @param trackId identificador da faixa.
     * @return os bytes do áudio.
     * @throws TrackNotFoundException se a faixa não existir no catálogo.
     */
    public byte[] listen(String trackId) {

        // Procura a faixa no catálogo
        Track track = catalog.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException(trackId));

        // Procura um proxy já existente para essa faixa, se não existir, cria um novo e guarda no mapa
        ProtectedAudioStreamProxy proxy = streams.computeIfAbsent(
                track.id(),
                id -> new ProtectedAudioStreamProxy(track, plan)
        );

        // Pede os bytes ao proxy, ele cuida da proteção, lazy loading e cache.
        return proxy.readBytes();
    }

    /**
     * Monta uma prévia da faixa com volume ajustado e fade in.
     *
     * @param trackId       identificador da faixa.
     * @param volume        fator de volume aplicado primeiro.
     * @param fadeInSamples quantidade de amostras do fade in, aplicado depois.
     * @return o áudio já decorado.
     * @throws TrackNotFoundException se a faixa não existir no catálogo.
     */
    public AudioTrack preview(String trackId, double volume, int fadeInSamples) {

        // Primeiro encontra a faixa para obter seus dados, principalmente o título
        Track track = catalog.findById(trackId)
                .orElseThrow(() -> new TrackNotFoundException(trackId));

        // Obtém os bytes através da operação listen().Assim, proteção e cache do Proxy continuam sendo utilizados.
        byte[] bytes = listen(trackId);

        // Cria um array de double com uma amostra para cada byte
        double[] samples = new double[bytes.length];

        // Converte cada byte em uma amostra dividindo por 128.0
        for (int i = 0; i < bytes.length; i++) {
            samples[i] = bytes[i] / 128.0;
        }

        // Cria o áudio original, ainda sem efeitos
        RawAudioTrack raw = new RawAudioTrack(track.title(), samples);

        // Primeiro aplica o volume
        AudioTrack withVolume = new VolumeEffect(raw, volume);

        // Depois aplica o fade in sobre o resultado do volume
        AudioTrack withFadeIn = new FadeInEffect(withVolume, fadeInSamples);

        // Devolve o áudio já decorado
        return withFadeIn;
    }
}