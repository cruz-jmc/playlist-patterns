package com.playlist.decorator;

/**
 * Efeito que aplica uma rampa linear de volume nas primeiras amostras.
 */
public final class FadeInEffect extends AudioEffect {

  // Quantidade de amostras utilizadas na rampa de fade in
  private final int sampleCount;

  /**
   * Cria o efeito de fade in.
   *
   * @param wrapped áudio decorado.
   * @param sampleCount quantidade de amostras usadas na rampa.
   */
  public FadeInEffect(AudioTrack wrapped, int sampleCount) {

    // Inicializa o decorator com o áudio que será decorado
    super(wrapped);

    // Construtor
    this.sampleCount = sampleCount;
  }

  @Override
  protected String describe() {

    // Monta a descrição textual do efeito
    return "fadeIn(" + sampleCount + ")";
  }

  @Override
  public double[] getSamples() {

    // Obtém as amostras do áudio decorado
    double[] original = wrapped.getSamples();

    // Cria um novo array para não modificar o áudio decorado
    double[] result = new double[original.length];

    // Percorre todas as amostras
    for (int i = 0; i < original.length; i++) {

      // Por padrão, a amostra permanece inalterada
      result[i] = original[i];

      // Se sampleCount for positivo e a amostra estiver entre as primeiras sampleCount amostras, aplica o fade.
      if (sampleCount > 0 && i < sampleCount) {

        // Calcula o fator da rampa. O cast para double evita divisão inteira.
        double factor = (double) i / sampleCount;

        // Aplica o fator à amostra
        result[i] = original[i] * factor;
      }
    }

    // Retorna o novo array com o efeito aplicado
    return result;
  }
}