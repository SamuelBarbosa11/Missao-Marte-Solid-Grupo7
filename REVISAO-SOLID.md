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
- [x] teste de limites dos inimigos: 20.000 rodadas com 3 inimigos em um mapa de -5 a 5, comparando a versão anterior e a corrigida. Antes: 59.912 posições fora do mapa e um inimigo a 159 casas da área jogável. Depois: 0 posições fora do mapa. O teste está versionado em [`test/solidexercicio10/TesteLimitesInimigos.java`](test/solidexercicio10/TesteLimitesInimigos.java) e pode ser repetido com os comandos da seção "Como executar o teste de limites"; como o sorteio é aleatório, os números do "antes" variam a cada execução (sempre acima de 59.000 posições fora do mapa), enquanto o "depois" é sempre 0;
- [x] reteste do aborto com `q` e de comandos inválidos após a correção: seis partidas no fácil com mapa mínimo (3x3, todas as células ocupadas) enviando três comandos inválidos e `q`, sem nenhuma colisão registrada depois do comando;
- [x] estatísticas de vitória com recorde: ranking vazio (sem recorde exibido), pontuação acima do primeiro colocado ("Novo recorde absoluto") e pontuação abaixo ("Recorde atual a ser batido"), além da mensagem de entrada no Top 5;
- [x] leitura do ranking com linhas malformadas (nome contendo `|` e pontuação não numérica): as linhas inválidas são ignoradas e o ranking válido continua sendo exibido.

### Como executar o teste de limites

```bash
javac -encoding UTF-8 -d out-teste src/solidexercicio10/model/*.java test/solidexercicio10/TesteLimitesInimigos.java
java -Dfile.encoding=UTF-8 -cp out-teste solidexercicio10.TesteLimitesInimigos
```

Saída de uma execução:

```text
Rodadas: 20000 | Inimigos: 3 | Mapa: -5 a 5
Antes  (tutorial):  59592 posições fora do mapa, maior distância da borda: 258
Depois (corrigido): 0 posições fora do mapa, maior distância da borda: 0
OK: nenhum inimigo saiu do mapa na versão corrigida.
```

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

### Rodada continuava depois do aborto (corrigido)

```text
Local: JogoService.jogarPartida
Princípio relacionado: preservação de comportamento na refatoração
Observação: ao receber q, o código marcava partidaAtiva = false, mas não saía do laço naquela rodada. Os inimigos ainda se moviam e a colisão ainda era verificada, então o jogador via "Missão abortada pelo piloto." seguido de "Alerta! Colisão detectada!". Com comando inválido acontecia algo parecido: o turno era consumido e os inimigos andavam. O original do exercicio10 usava break no q e continue no comando inválido.
Impacto: mensagem contraditória na tela e perda de vida por um comando que não deveria gerar turno. O teste manual de aborto passava porque o retorno ao menu funcionava; o defeito só aparecia quando havia um inimigo ao lado da nave.
Proposta: restaurar o break no q e o continue no comando inválido, como no original.
Prioridade: alta
```

### Recorde removido das estatísticas (corrigido)

```text
Local: JogoService.exibirEstatisticas
Princípio relacionado: preservação de comportamento na refatoração
Observação: o exercicio10 exibia, ao fim da vitória, o recorde atual a ser batido ou o aviso de novo recorde, e parabenizava o piloto que entrava no Top 5. O enunciado do Exercício 10 lista os recordes entre os requisitos. A versão do tutorial passou a mostrar apenas pontuação, movimentos, tempo e passageiros.
Impacto: uma funcionalidade exigida do jogo original deixou de existir sem registro.
Proposta: consultar o ranking pelo RankingRepository antes de salvar a partida e passar a lista para exibirEstatisticas, que volta a informar o recorde; a mensagem de Top 5 é exibida quando a pontuação supera o quinto colocado ou quando o ranking tem menos de cinco registros. O serviço continua dependendo só do contrato.
Prioridade: média
```

### Leitura do ranking frágil a linhas malformadas (corrigido)

```text
Local: repository.RankingService.listar
Princípio relacionado: SRP
Observação: o arquivo usa | como separador, mas o nome do piloto não é validado. Um nome como "Ana|B" grava uma linha com sete campos, e a leitura seguinte chamava Integer.parseInt sobre texto, encerrando o programa com NumberFormatException.
Impacto: uma única partida com nome incomum inutilizava a opção de ranking e, depois da restauração do recorde, também a tela de vitória.
Proposta: como o formato do arquivo é responsabilidade exclusiva do repositório, a tolerância fica nele: linhas que não têm exatamente seis campos ou que não convertem para número são ignoradas. O serviço não precisou mudar.
Prioridade: média
```

### Código sem uso na apresentação e no modelo

```text
Local: MapaRenderer.desenhar (sobrecargas de 1 e 3 argumentos), Nave(nome, x, y) e o aviso final de JogoService.posicionarPassageiros
Princípio relacionado: ISP
Observação: das três sobrecargas de desenhar, só a de sete argumentos é chamada; as outras duas fixam o mapa em -2 a 2, o que não corresponde a nenhum tamanho real de partida. O construtor de Nave sem capacidade também não é usado. Em posicionarPassageiros, o bloco que avisa "o mapa atual não suporta todos os passageiros" nunca executa, porque o while anterior só termina quando a quantidade foi atingida (se faltar espaço, sortearPosicaoLivre lança exceção antes).
Impacto: métodos públicos sem cliente ampliam a superfície das classes e sugerem usos que não existem; a sobrecarga com limites fixos, se chamada, desenharia um mapa diferente do jogado. O aviso inalcançável dá a impressão de um tratamento de erro que não acontece.
Proposta: remover as duas sobrecargas, o construtor sem capacidade e o bloco inalcançável. Foram mantidos nesta entrega por virem do código de referência do tutorial, e registrados aqui para a próxima iteração.
Prioridade: baixa
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

Por fim, foram restaurados dois comportamentos do original que a versão do
tutorial havia perdido. O comando `q` volta a encerrar a rodada imediatamente e
o comando inválido volta a não consumir turno, eliminando a colisão exibida
depois do aborto. As estatísticas de vitória voltam a mostrar o recorde e a
entrada no Top 5; para isso, `JogoService` consulta `RankingRepository.listar()`
antes de salvar a partida, sem passar a conhecer o arquivo. A leitura do
ranking também passou a ignorar linhas malformadas em vez de encerrar o
programa.
