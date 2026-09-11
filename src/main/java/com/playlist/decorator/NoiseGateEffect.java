package com.playlist.decorator;

/**
 * Efeito que zera amostras cujo valor absoluto fica abaixo de um limiar.
 */
public final class NoiseGateEffect extends AudioEffect {

  // Valor mínimo necessário para que uma amostra seja mantida.
  private final double threshold;

  /**
   * Cria o efeito de noise gate.
   *
   * @param wrapped áudio decorado.
   * @param threshold limiar de corte.
   */
  public NoiseGateEffect(AudioTrack wrapped, double threshold) {

    // Inicializa o decorator com o áudio que será decorado
    super(wrapped);

    // Construtor
    this.threshold = threshold;
  }

  @Override
  protected String describe() {

    // Formata o nome do efeito com duas casas decimais. Locale.ROOT garante "." como separador decimal
    return String.format(java.util.Locale.ROOT, "noiseGate(%.2f)", threshold);
  }

  @Override
  public double[] getSamples() {

    // Obtém as amostras já processadas pelo decorator anterior
    double[] original = wrapped.getSamples();

    // Cria um novo array para armazenar o resultado
    double[] result = new double[original.length];

    // Percorre todas as amostras
    for (int i = 0; i < original.length; i++) {

      // Verifica o valor absoluto da amostra
      if (Math.abs(original[i]) < threshold) {

        // Se estiver abaixo do limiar, elimina o ruído
        result[i] = 0.0;

      } else {

        // Caso contrário, mantém a amostra original
        result[i] = original[i];
      }
    }

    return result;
  }
}