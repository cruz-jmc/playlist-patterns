package com.playlist.decorator;

import java.util.Locale;

/**
 * Efeito que multiplica o volume das amostras, com corte em {@code [-1.0, 1.0]}.
 */
public final class VolumeEffect extends AudioEffect {

  // Fator utilizado para multiplicar cada amostra
  private final double factor;

  /**
   * Cria o efeito de volume.
   *
   * @param wrapped áudio decorado.
   * @param factor fator multiplicador do volume.
   */
  public VolumeEffect(AudioTrack wrapped, double factor) {

    // Inicializa o AudioEffect com o áudio que será decorado
    super(wrapped);

    // Construtor do fator de volume
    this.factor = factor;
  }

  @Override
  protected String describe() {

    // Formata o nome do efeito com uma casa decimal. Locale.ROOT garante que o separador decimal seja "."
    return String.format(Locale.ROOT, "volume(%.1f)", factor);
  }

  @Override
  public double[] getSamples() {

    // Obtém as amostras do áudio que está sendo decorado
    double[] original = wrapped.getSamples();

    // Cria um novo array para armazenar o resultado para não alterarmos as amostras do objeto decorado
    double[] result = new double[original.length];

    // Percorre todas as amostras
    for (int i = 0; i < original.length; i++) {

      // Multiplica a amostra pelo fator de volume
      double value = original[i] * factor;

      // Limita o resultado ao intervalo [-1.0, 1.0]
      value = Math.max(-1.0, Math.min(1.0, value));

      // Guarda a amostra processada no novo array
      result[i] = value;
    }

    return result;
  }
}