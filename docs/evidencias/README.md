# Evidências de teste

Transcrições reais das execuções do jogo `solidexercicio10` usadas na revisão
([REVISAO-SOLID.md](../../REVISAO-SOLID.md)). "Antes" é o commit `687b5f1`,
anterior às correções; "atual" é o código com todas as correções (commit
`ac75ffb`, indicado no cabeçalho de cada arquivo).

| Arquivo | O que comprova | Resultado |
|---|---|---|
| `01-vitoria-recorde-top5.txt` | Duas vitórias seguidas na mesma sessão: a 2ª mostra o recorde a ser batido (ou novo recorde) e ambas parabenizam pela entrada no Top 5; o ranking lista as duas | OK |
| `02-aborto-e-comando-invalido.txt` | Com `q` ou comando inválido os inimigos não se movem, logo não há colisão depois dessas mensagens | Antes: colisão logo após "Comando inválido." / "Missão abortada pelo piloto." (e até GAME OVER). Atual: 0 colisões e 0 exceções em 20 execuções (números no resumo do arquivo) |
| `03-mapa-minimo.txt` | Tamanho 1 no DIFICIL é ajustado para 2; tamanho 0 no FÁCIL (digitado com acento) é tratado como inválido e trocado pelo mínimo; 100 partidas por dificuldade no mapa mínimo, antes e depois da correção do sorteio | Antes: 18 de 100 partidas no fácil encerravam com `IllegalStateException`. Atual: 0 de 100 em todas as dificuldades (números no fim do arquivo) |
| `04-ranking-e-reset.txt` | Consulta do ranking gravado na evidência 01, reset e nova consulta vazia | OK |
| `05-ranking-malformado.txt` | Linhas malformadas no arquivo de ranking são ignoradas (só a linha válida aparece) | Atual: lista só a linha válida. Antes: `NumberFormatException` derruba o programa |
| `06-teste-limites-inimigos.txt` | Teste automatizado `TesteLimitesInimigos`: inimigos não saem do mapa | Atual: 0 posições fora do mapa (o movimento do tutorial, para comparação, sai do mapa) |

## Observações

- As entradas foram enviadas automaticamente pela entrada padrão. Nas
  partidas da evidência 01, os comandos eram escolhidos a partir do mapa
  impresso. Cada entrada aparece logo após o prompt que a consumiu, para a
  transcrição ter a aparência da tela; fora isso, a saída é a do programa, sem
  edição.
- Como os comandos chegavam em milissegundos, o "Tempo" das vitórias aparece
  como 0s.
- Mapas e movimentos dos inimigos são aleatórios: uma nova execução produz
  partidas diferentes, e no "antes" da evidência 02 pode levar mais de uma
  tentativa até o bug aparecer.
- O teste da evidência 06 pode ser repetido com os comandos de
  [REVISAO-SOLID.md](../../REVISAO-SOLID.md#como-executar-o-teste-de-limites).
