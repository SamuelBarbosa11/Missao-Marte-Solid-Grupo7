package solidexercicio10.service;

import java.util.List;
import java.util.Random;
import java.util.Scanner;
import solidexercicio10.model.Asteroide;
import solidexercicio10.model.Dificuldade;
import solidexercicio10.model.Engenheiro;
import solidexercicio10.model.Inimigo;
import solidexercicio10.model.Missao;
import solidexercicio10.model.Nave;
import solidexercicio10.model.Passageiro;
import solidexercicio10.model.Professor;
import solidexercicio10.presentation.MapaRenderer;
import solidexercicio10.repository.RankingEntry;
import solidexercicio10.repository.RankingRepository;

public class JogoService {

  private final RankingRepository rankingRepository;
  private final MapaRenderer mapaRenderer;
  private final Random random;

  public JogoService(RankingRepository rankingRepository) {
    this.rankingRepository = rankingRepository;
    this.mapaRenderer = new MapaRenderer();
    this.random = new Random();
  }

  public void executarLoop(Scanner scanner) {
    boolean rodando = true;
    while (rodando) {
      exibirMenu();
      String opcao = lerLinha(scanner, "Escolha uma opção: ", "1").trim();
      switch (opcao) {
        case "1":
          jogarPartida(scanner);
          break;
        case "2":
          exibirRanking();
          break;
        case "3":
          rankingRepository.limpar();
          System.out.println("Histórico de ranking removido.");
          break;
        case "4":
          rodando = false;
          System.out.println("\nObrigado por jogar a Missão Marte Unifor!");
          break;
        default:
          System.out.println("Opção inválida. Tente novamente.");
      }
    }
  }

  public void registrarPontuacao(String nome, int pontuacao) {
    rankingRepository.salvar(nome, pontuacao);
  }

  public List<RankingEntry> listarRanking() {
    return rankingRepository.listar();
  }

  private void exibirMenu() {
    System.out.println("\n--- MENU PRINCIPAL ---");
    System.out.println("1. Iniciar Nova Missão");
    System.out.println("2. Visualizar Ranking Top 5");
    System.out.println("3. Resetar Ranking");
    System.out.println("4. Sair");
    System.out.println("----------------------");
  }

  private void jogarPartida(Scanner scanner) {
    String pilotoNome = lerLinha(
      scanner,
      "\nDigite o nome do piloto: ",
      "Piloto Anônimo"
    ).trim();
    if (pilotoNome.isEmpty()) {
      pilotoNome = "Piloto Anônimo";
    }

    Dificuldade dificuldade = lerDificuldade(scanner);
    int tamanhoMapa = lerTamanhoMapa(scanner, dificuldade);
    int minX = -tamanhoMapa;
    int maxX = tamanhoMapa;
    int minY = -tamanhoMapa;
    int maxY = tamanhoMapa;

    System.out.println(
      "\nIniciando missão na dificuldade " + dificuldade + "..."
    );
    lerLinha(scanner, "Pressione Enter para decolar!", "");

    Missao missao = criarNovaMissao(dificuldade, minX, maxX, minY, maxY);
    Nave nave = missao.getNave();
    int score = definirPontuacaoInicial(dificuldade);
    int movimentos = 0;
    boolean partidaAtiva = true;
    long tempoInicio = System.currentTimeMillis();

    while (partidaAtiva) {
      mapaRenderer.desenhar(missao, score, pilotoNome, minX, maxX, minY, maxY);
      int passageirosABordo = nave.getPassageiros().size();
      int passageirosRestantes = missao.getPassageiros().size();
      int totalPassageiros = passageirosABordo + passageirosRestantes;
      System.out.printf(
        "Nave em (%d,%d) | Pontos: %d | Vidas: %d | A bordo: %d/%d | Restantes no mapa: %d | Total: %d%n",
        nave.getX(),
        nave.getY(),
        score,
        nave.getVidas(),
        passageirosABordo,
        nave.getCapacidade(),
        passageirosRestantes,
        totalPassageiros
      );

      String entrada = lerLinha(scanner, "Comando (w/s/a/d/c/q): ", "")
        .trim()
        .toLowerCase();
      if (entrada.isEmpty()) {
        continue;
      }

      char cmd = entrada.charAt(0);
      if (cmd == 'q') {
        System.out.println("Missão abortada pelo piloto.");
        partidaAtiva = false;
      } else if (cmd == 'c') {
        Passageiro passageiro = missao.passagemNaPosicao();
        if (passageiro == null) {
          System.out.println("Nenhum passageiro nesta posição.");
        } else {
          boolean embarcou = missao.embarcarPassageiroNaPosicao();
          if (embarcou) {
            score += passageiro.getPontuacao();
            System.out.printf(
              "Passageiro %s embarcado com sucesso! +%d pontos!%n",
              passageiro.getNome(),
              passageiro.getPontuacao()
            );
          } else {
            System.out.println(
              "Nave cheia! Não há espaço para mais passageiros."
            );
          }
        }
      } else if (cmd == 'w' || cmd == 's' || cmd == 'a' || cmd == 'd') {
        nave.moverComLimites(cmd, minX, maxX, minY, maxY);
        score--;
        movimentos++;
      } else {
        System.out.println("Comando inválido.");
      }

      missao.moverInimigos();
      if (missao.verificaColisao()) {
        nave.perderVida();
        if (nave.getVidas() > 0) {
          System.out.printf(
            "Alerta! Colisão detectada! Vidas restantes: %d%n",
            nave.getVidas()
          );
        } else {
          System.out.println("GAME OVER! A nave foi destruída.");
          partidaAtiva = false;
        }
      }

      if (score <= 0 && partidaAtiva) {
        System.out.println("Combustível/Pontuação zerada! Missão perdida.");
        partidaAtiva = false;
      }

      if (missao.todosEmbarcados() && partidaAtiva) {
        if (nave.getX() == 0 && nave.getY() == 0) {
          long tempoFim = System.currentTimeMillis();
          long tempoJogoSegundos = (tempoFim - tempoInicio) / 1000;
          System.out.println(
            "\n================================================================"
          );
          System.out.println(
            "🚀 DECOLAGEM AUTORIZADA! Nave acoplada à plataforma em (0,0)."
          );
          System.out.println(
            "Retornando à órbita marciana com todos os passageiros. Missão cumprida!"
          );
          System.out.println(
            "================================================================\n"
          );
          exibirEstatisticas(
            score,
            movimentos,
            tempoJogoSegundos,
            nave.getPassageiros().size()
          );
          rankingRepository.salvar(
            pilotoNome,
            score,
            dificuldade,
            nave.getPassageiros().size(),
            tempoJogoSegundos
          );
          partidaAtiva = false;
        } else {
          System.out.println(
            "✨ ALERTA: Todos os passageiros resgatados! Retorne para a Plataforma de Pouso '🔳' em (0,0) para completar a missão."
          );
        }
      }
    }
  }

  private Dificuldade lerDificuldade(Scanner scanner) {
    System.out.print("Escolha a Dificuldade (facil/medio/dificil): ");
    String valor = lerLinha(scanner, "", "medio").trim();
    return Dificuldade.deString(valor);
  }

  private int lerTamanhoMapa(Scanner scanner, Dificuldade dificuldade) {
    int tamanhoMinimo = tamanhoMinimoMapa(dificuldade);
    try {
      int tamanho = Integer.parseInt(
        lerLinha(scanner, "Tamanho do mapa (ex: 5): ", "5")
      );
      if (tamanho <= 0) {
        System.out.println(
          "Tamanho inválido, usando o tamanho mínimo disponível."
        );
        return tamanhoMinimo;
      }
      if (tamanho < tamanhoMinimo) {
        System.out.printf(
          "O tamanho mínimo para %s é %d; usando esse valor.%n",
          dificuldade,
          tamanhoMinimo
        );
        return tamanhoMinimo;
      }
      return tamanho;
    } catch (NumberFormatException e) {
      System.out.println(
        "Entrada inválida, usando o tamanho mínimo disponível."
      );
      return tamanhoMinimo;
    }
  }

  private int tamanhoMinimoMapa(Dificuldade dificuldade) {
    int entidades = switch (dificuldade) {
      case FACIL -> 8;
      case MEDIO -> 9;
      case DIFICIL -> 12;
    };
    int tamanho = 1;
    while ((2 * tamanho + 1) * (2 * tamanho + 1) - 1 < entidades) {
      tamanho++;
    }
    return tamanho;
  }

  private int definirPontuacaoInicial(Dificuldade dificuldade) {
    return switch (dificuldade) {
      case FACIL -> 30;
      case DIFICIL -> 15;
      default -> 20;
    };
  }

  private Missao criarNovaMissao(
    Dificuldade dificuldade,
    int minX,
    int maxX,
    int minY,
    int maxY
  ) {
    int qtdPassageiros = 4;
    int qtdAsteroides = 2;
    int qtdInimigos = 2;

    if (dificuldade == Dificuldade.MEDIO) {
      qtdPassageiros = 5;
    } else if (dificuldade == Dificuldade.DIFICIL) {
      qtdPassageiros = 6;
      qtdAsteroides = 3;
      qtdInimigos = 3;
    }

    Nave nave = new Nave("A-1", 0, 0, qtdPassageiros);
    Missao missao = new Missao(nave);

    posicionarPassageiros(missao, qtdPassageiros, minX, maxX, minY, maxY, nave);

    posicionarEntidades(
      missao,
      qtdAsteroides,
      minX,
      maxX,
      minY,
      maxY,
      nave,
      true
    );
    posicionarEntidades(
      missao,
      qtdInimigos,
      minX,
      maxX,
      minY,
      maxY,
      nave,
      false
    );

    return missao;
  }

  private void posicionarPassageiros(
    Missao missao,
    int qtdPassageiros,
    int minX,
    int maxX,
    int minY,
    int maxY,
    Nave nave
  ) {
    int indice = 0;
    while (missao.getPassageiros().size() < qtdPassageiros) {
      int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY, nave);
      int x = posicao[0];
      int y = posicao[1];
      if (indice % 3 == 0) {
        missao.adicionarPassageiro(new Professor("Dr. Silva", x, y));
      } else if (indice % 3 == 1) {
        missao.adicionarPassageiro(new Engenheiro("Eng. Rosa", x, y));
      } else {
        missao.adicionarPassageiro(new Professor("Dr. Lima", x, y));
      }
      indice++;
    }

    if (missao.getPassageiros().size() < qtdPassageiros) {
      System.out.printf(
        "Aviso: o mapa atual não suporta todos os passageiros da dificuldade escolhida (%d/%d).%n",
        missao.getPassageiros().size(),
        qtdPassageiros
      );
    }
  }

  private void posicionarEntidades(
    Missao missao,
    int qtd,
    int minX,
    int maxX,
    int minY,
    int maxY,
    Nave nave,
    boolean asteroide
  ) {
    int total = asteroide
      ? missao.getAsteroides().size()
      : missao.getInimigos().size();
    while (total < qtd) {
      int[] posicao = sortearPosicaoLivre(missao, minX, maxX, minY, maxY, nave);
      if (asteroide) {
        missao.adicionarAsteroide(new Asteroide(posicao[0], posicao[1]));
      } else {
        missao.adicionarInimigo(new Inimigo(posicao[0], posicao[1]));
      }
      total++;
    }
  }

  private int[] sortearPosicaoLivre(
    Missao missao,
    int minX,
    int maxX,
    int minY,
    int maxY,
    Nave nave
  ) {
    int tentativasMaximas = Math.max(
      20,
      (maxX - minX + 1) * (maxY - minY + 1) * 2
    );
    for (int tentativa = 0; tentativa < tentativasMaximas; tentativa++) {
      int x = random.nextInt(maxX - minX + 1) + minX;
      int y = random.nextInt(maxY - minY + 1) + minY;
      if (
        !posicaoOcupada(missao, x, y) && !(x == nave.getX() && y == nave.getY())
      ) {
        return new int[] { x, y };
      }
    }
    throw new IllegalStateException(
      "O mapa nao possui posicoes livres suficientes"
    );
  }

  private boolean posicaoOcupada(Missao missao, int x, int y) {
    for (Passageiro passageiro : missao.getPassageiros()) {
      if (passageiro.getX() == x && passageiro.getY() == y) {
        return true;
      }
    }
    for (Asteroide asteroide : missao.getAsteroides()) {
      if (asteroide.getX() == x && asteroide.getY() == y) {
        return true;
      }
    }
    for (Inimigo inimigo : missao.getInimigos()) {
      if (inimigo.getX() == x && inimigo.getY() == y) {
        return true;
      }
    }
    return false;
  }

  private void exibirRanking() {
    List<RankingEntry> ranking = rankingRepository.listar();
    if (ranking.isEmpty()) {
      System.out.println("Nenhum registro de ranking ainda.");
      return;
    }
    System.out.println("\n=== TOP 5 DO RANKING ===");
    for (int i = 0; i < Math.min(5, ranking.size()); i++) {
      RankingEntry entry = ranking.get(i);
      System.out.printf(
        "%d. %s | Pontos: %d | Dif.: %s | Passageiros: %d | %s | Tempo: %ds%n",
        i + 1,
        entry.name,
        entry.score,
        entry.dificuldade,
        entry.passageirosColetados,
        entry.dataHora,
        entry.tempoJogo
      );
    }
  }

  private void exibirEstatisticas(
    int score,
    int movimentos,
    long tempoJogoSegundos,
    int passageirosColetados
  ) {
    System.out.println("\n=== ESTATÍSTICAS DA MISSÃO ===");
    System.out.printf("Pontuação final: %d%n", score);
    System.out.printf("Movimentos realizados: %d%n", movimentos);
    System.out.printf("Tempo de missão: %d segundos%n", tempoJogoSegundos);
    System.out.printf("Passageiros resgatados: %d%n", passageirosColetados);
  }

  private String lerLinha(
    Scanner scanner,
    String mensagem,
    String valorPadrao
  ) {
    if (!mensagem.isEmpty()) {
      System.out.print(mensagem);
    }
    String entrada = scanner.nextLine();
    if (entrada == null || entrada.isBlank()) {
      return valorPadrao;
    }
    return entrada;
  }
}
