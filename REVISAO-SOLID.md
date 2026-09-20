# Revisão final da atividade

## 1. Tabela de Contribuições

| Integrante            | Matrícula | Usuário Git     | Exercícios e funcionalidades                                            |
| --------------------- | --------- | --------------- | ----------------------------------------------------------------------- |
| Samuel Miguel Barbosa | 2517428   | SamuelBarbosa11 | Refatoração principal, `Main`, integração das camadas e validação geral |
| João Gabriel Rinaldi  | 2510365   | JgRM0           | Modelos, SOLID, UML e interfaces                                        |
| Rafael Dantas         | 2517979   | r-dantas9       | Documentação, testes manuais e evidências                               |

---

Data: 24.09.26

## Como validei a solução

Registre os comandos executados e os fluxos testados:

- [x] compilação do código inicial com `javac -encoding UTF-8 -d out-original`;
- [x] compilação da versão refatorada com `javac -encoding UTF-8 -d out`;
- [x] início de uma missão e renderização do mapa;
- [x] aborto voluntário da missão e retorno ao menu;
- [x] movimentação, embarque e conclusão da missão;
- [x] consulta e reset do ranking;
- [x] execução pelo `run.cmd start` e saída imediata;
- [x] teste de mapa insuficiente na dificuldade difícil, corrigido com tamanho mínimo automático;
- [x] teste de limites dos inimigos: 20.000 rodadas com 3 inimigos em um mapa de -5 a 5, comparando a versão anterior e a corrigida. Antes: 59.912 posições fora do mapa e um inimigo a 159 casas da área jogável. Depois: 0 posições fora do mapa.

## Achados da revisão

### Inimigos saindo dos limites do mapa (corrigido)

```text
Local: Missao.moverInimigos e JogoService (chamada do movimento)
Princípio relacionado: preservação de comportamento na refatoração
Observação: o método sorteava o deslocamento e chamava inimigo.mover(dx, dy) sem consultar os limites do mapa. O código original do exercicio10 recebia minX, maxX, minY e maxY e bloqueava o passo que ultrapassasse a borda.
Impacto: os inimigos deixavam a área jogável nas primeiras rodadas e nunca retornavam, de modo que a missão perdia os perigos móveis e ficava mais fácil do que o previsto pela dificuldade escolhida.
Proposta: devolver os limites à assinatura de moverInimigos e zerar o deslocamento que ultrapassaria a borda, mantendo o sorteio aleatório como estava.
Prioridade: alta
```

### Mapa pequeno e posições insuficientes

```text
Local: JogoService.lerTamanhoMapa e criação da missão
Princípio relacionado: SRP e robustez da regra de negócio
Observação: mapas pequenos não comportavam todos os passageiros e perigos e causavam IllegalStateException.
Impacto: a aplicação encerrava durante o início da missão, sem permitir ao usuário jogar.
Proposta: calcular o tamanho mínimo por dificuldade e ajustar entradas menores antes da geração aleatória.
Prioridade: alta
```

### Responsabilidade do serviço

```text
Local: JogoService
Princípio relacionado: SRP
Observação: o serviço coordena menu, entrada, criação da missão, pontuação e encerramento.
Impacto: a integração fica simples para o projeto atual, mas a classe tende a crescer com novas regras.
Proposta: manter como está neste escopo; extrair uma MissaoFactory e um serviço de pontuação se o jogo crescer.
Prioridade: baixa
```

### Criação de passageiros fixa no serviço

```text
Local: JogoService.posicionarPassageiros e model.Astronauta
Princípio relacionado: OCP
Observação: o embarque e a pontuação são polimórficos e aceitam qualquer subtipo de Passageiro, mas a criação continua fixa no serviço: posicionarPassageiros alterna Professor, Engenheiro e Professor por indice % 3. Astronauta existe no modelo, tem pontuação e símbolo próprios e aparece na legenda do mapa, mas nunca é instanciado.
Impacto: incluir um novo tipo de passageiro exige alterar o fluxo principal, e não apenas estender o domínio; o tipo não utilizado dá falsa impressão de extensibilidade e não é exercitado por nenhum teste.
Proposta: extrair a criação para uma fábrica (PassageiroFactory) ou receber os tipos disponíveis no construtor do serviço, e incluir Astronauta na distribuição.
Prioridade: média
```

### Tipo do passageiro guardado como texto

```text
Local: model.Passageiro e subclasses
Princípio relacionado: LSP
Observação: tornar Passageiro abstrata com getPontuacao() abstrato corrigiu o problema da versão original, em que a base era concreta e devolvia 10 pontos por padrão. Resta um ponto frágil: o campo tipo é uma String recebida pelo construtor e não tem vínculo com a classe real, então nada impede uma subclasse de se declarar com o tipo de outra.
Impacto: qualquer regra ou relatório que compare getTipo() pode divergir do tipo real do objeto, e a inconsistência só aparece em tempo de execução.
Proposta: derivar o rótulo da própria classe (getClass().getSimpleName()) ou transformá-lo em método abstrato ao lado de getPontuacao(), eliminando o estado redundante.
Prioridade: baixa
```

### Interface Movel sem cliente na Nave

```text
Local: model.Movel e model.Nave
Princípio relacionado: ISP
Observação: a separação entre Posicionavel e Movel é adequada e evita que Asteroide e Passageiro precisem de um mover() vazio. Porém Nave implementa Movel sem que nenhum cliente use esse método: JogoService movimenta a nave apenas por moverComLimites e Missao.moverInimigos chama mover somente nos inimigos. Nave.mover também ignora os limites do mapa.
Impacto: método público sem cliente amplia a superfície da classe, precisa ser mantido e pode ser chamado por engano, tirando a nave da área jogável.
Proposta: remover implements Movel de Nave, ou fazer moverComLimites validar os limites e delegar a mover, eliminando a duplicação do cálculo de posição.
Prioridade: média
```

### Contrato do ranking com operação sem cliente

```text
Local: repository.RankingRepository, repository.RankingService e JogoService
Princípio relacionado: DIP
Observação: a inversão está correta. JogoService recebe RankingRepository pelo construtor, nunca importa RankingService, e o Main é o único ponto que escolhe a implementação concreta. O que destoa é o tamanho do contrato: RankingRepository declara duas sobrecargas de salvar, e a de dois argumentos não é usada por ninguém. Seu único chamador seria JogoService.registrarPontuacao, que por sua vez também não é invocado em lugar nenhum; o mesmo vale para JogoService.listarRanking.
Impacto: trocar a persistência por banco ou memória continua barato, mas toda nova implementação do contrato é obrigada a implementar uma operação que nenhum cliente chama, e os dois métodos mortos no serviço sugerem um uso que não existe.
Proposta: manter a inversão como está e remover a sobrecarga salvar(nome, pontuacao) do contrato, junto com registrarPontuacao e listarRanking no serviço, deixando na interface apenas o que os clientes realmente usam.
Prioridade: baixa
```

## Decisões com as quais concordo

Manter `RankingRepository` como abstração foi uma boa decisão. `JogoService` não
precisa conhecer o arquivo nem a implementação concreta, o que facilita trocar
a persistência e criar testes com um repositório em memória.

## Decisões com as quais não concordo

A principal discordância é com o `moverInimigos()` proposto pelo tutorial em
`src/README.md`. A versão do tutorial sorteia o deslocamento e chama
`inimigo.mover(dx, dy)` sem consultar os limites do mapa, enquanto o código
original do `exercicio10` recebia `minX`, `maxX`, `minY` e `maxY` e bloqueava o
passo que ultrapassasse a borda. Seguir o tutorial removeu essa proteção, e a
consequência é mensurável: no teste de 20.000 rodadas registrado acima, a
versão do tutorial acumulou 59.912 posições fora do mapa e levou um inimigo a
159 casas da área jogável. Como os inimigos nunca retornam, a partida perde os
perigos móveis logo no início.

O ponto não é de estilo, e sim de objetivo da atividade: a refatoração deve
preservar o comportamento da aplicação, e neste trecho ela não preservou. A
simplificação da assinatura deixou o método mais curto, mas transferiu para
lugar nenhum uma regra que o domínio precisava manter.

A geração aleatória da missão ainda fica dentro de `JogoService`. Para o
projeto atual isso evita abstrações desnecessárias, mas eu a extrairia para uma
`MissaoFactory` caso surgissem mais mapas, cenários ou regras de distribuição.

## Melhoria implementada (opcional)

Foi implementada a validação do tamanho mínimo do mapa em
`JogoService.lerTamanhoMapa`. Antes, uma missão difícil em um mapa de tamanho
1 terminava com `IllegalStateException` por falta de posições livres. Depois,
o programa calcula o mínimo necessário por dificuldade, informa o ajuste e
continua a execução normalmente.

Também foi corrigido o escape dos inimigos. `Missao.moverInimigos` passou a
receber os limites do mapa e a zerar o deslocamento que ultrapassaria a borda;
o sorteio aleatório permanece o mesmo. Depois da correção, o mesmo teste de
20.000 rodadas registrou zero posições fora do mapa. Os limites ficaram na
`Missao`, e não dentro de `Inimigo.mover`, para que `Movel.mover(dx, dy)`
continue sendo um deslocamento relativo puro: quem conhece o tabuleiro é a
missão, não a entidade que se desloca.
