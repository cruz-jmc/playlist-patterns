package com.playlist.adapter;

import com.playlist.adapter.external.LegacyVinylCatalog;
import com.playlist.core.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter que converte os registros do {@link LegacyVinylCatalog} para {@link Track}.
 */
public class VinylCatalogAdapter implements TrackCatalog {

    // Referência para o sistema legado que será adaptado.
    private final LegacyVinylCatalog legacyCatalog;

    /**
     * Cria o adapter em cima do sistema legado.
     *
     * @param legacyCatalog catálogo legado a ser adaptado. Não pode ser nulo.
     * @throws IllegalArgumentException se o catálogo for nulo.
     */
    public VinylCatalogAdapter(LegacyVinylCatalog legacyCatalog) {
        // Verifica se o catálogo foi fornecido:
        if (legacyCatalog == null) {
            throw new IllegalArgumentException("O catálogo legado não pode ser nulo.");
        }

        // construtor normal
        this.legacyCatalog = legacyCatalog;
    }

    @Override
    public List<Track> findAll() {
        // cria uma lista para armazenar apenas faixas válidas
        List<Track> tracks = new ArrayList<>();

        // pega todos os registros em formato de texto
        String[] records = legacyCatalog.fetchAllRecords();

        // percorre os registros recebidos:
        for (String record : records) {
            // tenta converter em uma Track:
            Optional<Track> track = parseRecord(record);

            // só adiciona a track quando a conversão for feita
            track.ifPresent(tracks::add);
        }

        return tracks;
    }

    @Override
    public Optional<Track> findById(String id) {
        // IDs nulos ou em branco não podem ser encontrados.
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        String record = legacyCatalog.findRecordByCatalogNumber(id);

        if (record == null) {
            return Optional.empty();
        }

        return parseRecord(record);
    }

    /**
     * Converte um registro legado em uma Track.
     *
     * @param record registro no formato legado.
     * @return a Track convertida ou Optional.empty() se o registro for inválido.
     */

    private Optional<Track> parseRecord(String record) {

        if (record == null) {
            return Optional.empty();
        }

        // Divide o registro nos cinco campos separados por '|'. O -1 faz o split preservar campos vazios, inclusive no final.
        String[] fields = record.split("\\|", -1);

        // O registro precisa possuir exatamente 5 campos
        if (fields.length != 5) {
            return Optional.empty();
        }

        // Remove os espaços das pontas
        String id = fields[0].trim();
        String title = fields[1].trim();
        String artist = fields[2].trim();
        String durationField = fields[3].trim();
        String premiumField = fields[4].trim();

        // ID vazio = registro inválido
        if (id.isEmpty()) {
            return Optional.empty();
        }

        // título vazio também
        if (title.isEmpty()) {
            return Optional.empty();
        }

        // converte para segundos
        int durationSeconds;

        try {
            long durationMilliseconds = Long.parseLong(durationField);

            if (durationMilliseconds < 0) {
                return Optional.empty();
            }

            long seconds = durationMilliseconds / 1000;

            if (seconds > Integer.MAX_VALUE) {
                return Optional.empty();
            }

            durationSeconds = (int) seconds;
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }

        String formattedTitle = formatWords(title);

        String formattedArtist = formatArtist(artist);

        boolean premium = premiumField.equalsIgnoreCase("Y");

        return Optional.of(new Track(id, formattedTitle, formattedArtist, durationSeconds, premium));
    }

    /**
     * Converte um texto em caixa alta para o formato com a primeira letra
     * de cada palavra em maiúscula e o restante em minúsculo.
     *
     * @param text texto a ser convertido.
     * @return texto formatado.
     */

    private String formatWords(String text) {

        String normalized = text.trim().replaceAll("\\s+", " ");

        String[] words = normalized.split(" ");

        StringBuilder result = new StringBuilder();

        for (String word : words) {
            // Ignora palavras vazias, caso existam.
            if (word.isEmpty()) {
                continue;
            }

            String firstLetter = word.substring(0, 1).toUpperCase();
            String remaining = word.substring(1).toLowerCase();

            if (result.length() > 0) {
                result.append(" ");
            }

            result.append(firstLetter).append(remaining);
        }

        return result.toString();
    }

    /**
     * Converte o artista do formato legado "SOBRENOME, NOME"
     * para o formato interno "Nome Sobrenome".
     *
     * @param artist artista no formato legado.
     * @return artista no formato interno.
     */

    private String formatArtist(String artist) {

        String[] parts = artist.split(",", -1);

        if (parts.length != 2) {
            return formatWords(artist);
        }

        String surname = parts[0].trim();
        String name = parts[1].trim();

        String formattedName = formatWords(name);
        String formattedSurname = formatWords(surname);

        return formattedName + " " + formattedSurname;
    }
}