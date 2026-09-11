package com.playlist.decorator;

/**
 * Decorator abstrato: envolve outro {@link AudioTrack} e acrescenta um efeito.
 */
public abstract class AudioEffect implements AudioTrack {

  /**
   * O áudio decorado por este efeito.
   */
  protected final AudioTrack wrapped;

  /**
   * Guarda o áudio que será decorado.
   *
   * @param wrapped áudio decorado. Não pode ser nulo.
   * @throws IllegalArgumentException se {@code wrapped} for nulo.
   */
  protected AudioEffect(AudioTrack wrapped) {

    // Verifica se o áudio que será "decorado" foi fornecido
    if (wrapped == null) {
      throw new IllegalArgumentException("wrapped não pode ser nulo");
    }

    // Guarda no construtor normal
    this.wrapped = wrapped;
  }

  /**
   * Nome do efeito, já formatado, usado na cadeia de efeitos.
   *
   * @return por exemplo {@code "volume(2.0)"}.
   */
  protected abstract String describe();

  @Override
  public String getTitle() {

    // O título pertence ao áudio original, então simplesmente repassamos a chamada.
    return wrapped.getTitle();
  }

  @Override
  public String getEffectChain() {

    // Pega a cadeia que já existia no áudio decorado e acrescenta o efeito atual no final, o "wrapped"
    return wrapped.getEffectChain() + " -> " + describe();
  }
}