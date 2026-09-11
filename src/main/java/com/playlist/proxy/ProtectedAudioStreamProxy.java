package com.playlist.proxy;

import com.playlist.core.AccessDeniedException;
import com.playlist.core.Subscription;
import com.playlist.core.Track;
import java.util.function.Supplier;

/**
 * Proxy que controla o acesso ao {@link RemoteAudioStream}.
 * Ele acumula três responsabilidades clássicas do padrão: proteção
 * (bloqueia faixas premium para o plano gratuito), lazy loading (só cria o
 * objeto real quando o áudio é realmente pedido) e cache (não baixa o mesmo
 * áudio duas vezes).
 */
public class ProtectedAudioStreamProxy implements AudioStream {

  // Faixa associada ao áudio que será protegido
  private final Track track;

  // Plano de assinatura do usuário que está tentando ouvir a faixa
  private final Subscription plan;

  // Fábrica responsável por criar o objeto real somente quando necessário
  private final Supplier<AudioStream> loader;

  // Referência para o objeto real
  // Começa como null por causa do lazy load
  private AudioStream realStream;

  // Cache dos bytes já baixados.
  // Começa como null porque nenhum áudio foi baixado inicialmente.
  private byte[] cachedBytes;

  /**
   * Cria o proxy com uma fábrica explícita do objeto real.
   *
   * @param track faixa que será transmitida.
   * @param plan plano de assinatura de quem está ouvindo.
   * @param loader fábrica que cria o stream real. Só pode ser chamada quando o
   *     áudio for realmente necessário.
   * @throws IllegalArgumentException se qualquer argumento for nulo.
   */
  public ProtectedAudioStreamProxy(
          Track track,
          Subscription plan,
          Supplier<AudioStream> loader) {

    // Verifica se algum dos argumentos obrigatórios é nulo:
    if (track == null || plan == null || loader == null) {
      throw new IllegalArgumentException("Nenhum argumento pode ser nulo.");
    }

    // Construtor base
    this.track = track;
    this.plan = plan;
    this.loader = loader;
  }

  /**
   * Cria o proxy usando {@link RemoteAudioStream} como objeto real.
   *
   * @param track faixa que será transmitida.
   * @param plan plano de assinatura de quem está ouvindo.
   */
  public ProtectedAudioStreamProxy(Track track, Subscription plan) {

    // Delega para o construtor principal. A lambda não cria o RemoteAudioStream agora, ela apenas define como criá-lo quando loader.get() for chamado.
    this(track, plan, () -> new RemoteAudioStream(track));
  }

  /**
   * Indica se o objeto real já foi criado.
   *
   * @return {@code true} apenas depois que o stream real tiver sido carregado.
   */
  public boolean isLoaded() {

    // O objeto real existe somente depois que realStream for diferente de null
    return realStream != null;
  }

  @Override
  public String getTrackId() {

    // Obtém o ID diretamente da Track, isso não acessa realStream por isso não dispara o lazy loading.
    return track.id();
  }

  /**
   * Devolve os bytes do áudio, respeitando plano, carga preguiçosa e cache.
   *
   * @return uma cópia dos bytes do áudio.
   * @throws AccessDeniedException se a faixa for premium e o plano for
   *     {@link Subscription#FREE}.
   */
  @Override
  public byte[] readBytes() {

    // Verifica se a faixa é premium e o usuário está no plano gratuito
    if (track.premium() && plan == Subscription.FREE) {

      // Bloqueia o acesso antes de criar o objeto real
      throw new AccessDeniedException("Acesso negado à faixa premium.");
    }

    // Se já temos os bytes no cache, não precisamos acessar o objeto real
    if (cachedBytes != null) {

      // Retorna uma cópia para impedir que o chamador altere o cache
      return cachedBytes.clone();
    }

    // O objeto real ainda não existe
    if (realStream == null) {

      // Cria o objeto real somente agora
      realStream = loader.get();

      // Garante que um loader inválido não deixe o Proxy em estado inconsistente
      if (realStream == null) {
        throw new IllegalStateException("O loader retornou um stream nulo.");
      }
      // -> Ela não é explicitamente exigida pelo README.md, mas ela protege contra um Supplier que, indevidamente, retorne null.
    }

    // Faz a transferência real do áudio
    byte[] downloadedBytes = realStream.readBytes();

    // Guarda uma cópia dos bytes no cache
    cachedBytes = downloadedBytes.clone();

    // Retorna outra cópia para proteger o cache contra alterações externas
    return cachedBytes.clone();
  }
}