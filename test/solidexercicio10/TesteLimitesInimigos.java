package solidexercicio10;

import java.util.List;
import solidexercicio10.model.Inimigo;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;

/**
 * Teste de limites dos inimigos citado em REVISAO-SOLID.md.
 *
 * Executa 20.000 rodadas com 3 inimigos em um mapa de -5 a 5 e compara:
 * - antes: o moverInimigos() do tutorial, que desloca sem consultar a borda;
 * - depois: Missao.moverInimigos(minX, maxX, minY, maxY), da versão corrigida.
 *
 * O sorteio usa Math.random(), então os números do "antes" variam a cada
 * execução. O "depois" deve registrar sempre 0 posições fora do mapa.
 */
public class TesteLimitesInimigos {

  private static final int RODADAS = 20_000;
  private static final int QTD_INIMIGOS = 3;
  private static final int MIN = -5;
  private static final int MAX = 5;

  public static void main(String[] args) {
    Resultado antes = executar(false);
    Resultado depois = executar(true);

    System.out.printf(
      "Rodadas: %d | Inimigos: %d | Mapa: %d a %d%n",
      RODADAS,
      QTD_INIMIGOS,
      MIN,
      MAX
    );
    System.out.printf(
      "Antes  (tutorial):  %d posições fora do mapa, maior distância da borda: %d%n",
      antes.foraDoMapa,
      antes.maiorDistancia
    );
    System.out.printf(
      "Depois (corrigido): %d posições fora do mapa, maior distância da borda: %d%n",
      depois.foraDoMapa,
      depois.maiorDistancia
    );

    if (depois.foraDoMapa != 0) {
      System.out.println("FALHOU: inimigos saíram do mapa na versão corrigida.");
      System.exit(1);
    }
    System.out.println("OK: nenhum inimigo saiu do mapa na versão corrigida.");
  }

  private static Resultado executar(boolean comLimites) {
    Missao missao = new Missao(new Nave("Teste", 0, 0, 0));
    missao.adicionarInimigo(new Inimigo(MAX, MAX));
    missao.adicionarInimigo(new Inimigo(MIN, MIN));
    missao.adicionarInimigo(new Inimigo(MAX, MIN));

    Resultado resultado = new Resultado();
    for (int rodada = 0; rodada < RODADAS; rodada++) {
      if (comLimites) {
        missao.moverInimigos(MIN, MAX, MIN, MAX);
      } else {
        moverInimigosDoTutorial(missao.getInimigos());
      }
      for (Inimigo inimigo : missao.getInimigos()) {
        int distancia = Math.max(
          distanciaDaBorda(inimigo.getX()),
          distanciaDaBorda(inimigo.getY())
        );
        if (distancia > 0) {
          resultado.foraDoMapa++;
          resultado.maiorDistancia = Math.max(resultado.maiorDistancia, distancia);
        }
      }
    }
    return resultado;
  }

  // Cópia do moverInimigos() proposto em src/README.md, sem os limites do mapa.
  private static void moverInimigosDoTutorial(List<Inimigo> inimigos) {
    for (Inimigo inimigo : inimigos) {
      int dx = (int) (Math.random() * 3) - 1;
      int dy = (int) (Math.random() * 3) - 1;
      inimigo.mover(dx, dy);
    }
  }

  private static int distanciaDaBorda(int coordenada) {
    if (coordenada < MIN) {
      return MIN - coordenada;
    }
    if (coordenada > MAX) {
      return coordenada - MAX;
    }
    return 0;
  }

  private static class Resultado {

    int foraDoMapa;
    int maiorDistancia;
  }
}
