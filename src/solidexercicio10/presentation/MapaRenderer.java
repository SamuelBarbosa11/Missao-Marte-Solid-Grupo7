package solidexercicio10.presentation;

import solidexercicio10.model.Asteroide;
import solidexercicio10.model.Inimigo;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Passageiro;

public class MapaRenderer {

  public void desenhar(Missao missao) {
    desenhar(missao, 0, "Piloto", -2, 2, -2, 2);
  }

  public void desenhar(Missao missao, int score, String pilotoNome) {
    desenhar(missao, score, pilotoNome, -2, 2, -2, 2);
  }

  public void desenhar(
    Missao missao,
    int score,
    String pilotoNome,
    int minX,
    int maxX,
    int minY,
    int maxY
  ) {
    System.out.println();
    System.out.printf(
      "Mapa da Missão | Pontos: %d | Piloto: %s%n",
      score,
      pilotoNome
    );
    System.out.print("    ");
    for (int x = minX; x <= maxX; x++) {
      System.out.printf(" %2d", x);
    }
    System.out.println();
    System.out.print("    ");
    for (int x = minX; x <= maxX; x++) {
      System.out.print(" __");
    }
    System.out.println();

    for (int y = maxY; y >= minY; y--) {
      System.out.printf("%3d|", y);
      for (int x = minX; x <= maxX; x++) {
        String symbol = ".";
        if (missao.getNave().getX() == x && missao.getNave().getY() == y) {
          symbol = missao.getNave().getSimbolo();
        } else {
          for (Passageiro passageiro : missao.getPassageiros()) {
            if (passageiro.getX() == x && passageiro.getY() == y) {
              symbol = passageiro.getSimbolo();
              break;
            }
          }
          if (symbol.equals(".")) {
            for (Asteroide asteroide : missao.getAsteroides()) {
              if (asteroide.getX() == x && asteroide.getY() == y) {
                symbol = asteroide.getSimbolo();
                break;
              }
            }
          }
          if (symbol.equals(".")) {
            for (Inimigo inimigo : missao.getInimigos()) {
              if (inimigo.getX() == x && inimigo.getY() == y) {
                symbol = inimigo.getSimbolo();
                break;
              }
            }
          }
          if (symbol.equals(".") && x == 0 && y == 0) {
            symbol = "🔳";
          }
        }
        System.out.printf(" %2s", symbol);
      }
      System.out.println();
    }

    System.out.println(
      "Legenda: 🚀=Nave, 🔳=Plataforma, 🎓=Professor, 🔧=Engenheiro, 🔭=Astronauta, ☄️=Asteroide, 👾=Inimigo, .=Vazio"
    );
    System.out.println("Comandos: w/s/a/d (mover), c (embarcar), q (sair)");
  }
}
