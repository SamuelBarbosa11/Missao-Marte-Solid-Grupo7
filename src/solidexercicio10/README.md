# Missão Marte Unifor - Versão SOLID

Esta pasta contém a versão refatorada do jogo do Exercício 10. O código
original foi preservado em `src/exercicio10` para comparação. Nesta versão,
o ponto de entrada monta as dependências e o fluxo do jogo é distribuído entre
as camadas de serviço, domínio, apresentação e persistência.

## Objetivo

Manter o fluxo principal do jogo, reduzindo o acoplamento e separando
responsabilidades:

- `Main` inicia a aplicação e conecta as dependências.
- `JogoService` coordena menu, partida, pontuação e encerramento.
- `model` representa as entidades e regras do domínio.
- `MapaRenderer` desenha o estado da missão no console.
- `RankingRepository` define o contrato de persistência.
- `RankingService` grava e consulta o ranking em arquivo.

## Estrutura

```text
src/solidexercicio10/
	Main.java
	model/
		Asteroide.java
		Astronauta.java
		Dificuldade.java
		Engenheiro.java
		EntidadeMapa.java
		Inimigo.java
		Missao.java
		Movel.java
		Nave.java
		Passageiro.java
		Posicionavel.java
		Professor.java
	presentation/
		MapaRenderer.java
	repository/
		RankingEntry.java
		RankingRepository.java
		RankingService.java
	service/
		JogoService.java
```

## SOLID aplicado

- **SRP:** o fluxo do jogo, o modelo, a renderização e a persistência ficam
  em responsabilidades separadas.
- **OCP:** novos tipos de `Passageiro` podem herdar da classe base sem alterar
  a lógica de embarque e pontuação existente.
- **LSP:** `Professor`, `Engenheiro` e `Astronauta` podem ser tratados como
  `Passageiro`.
- **ISP:** `Posicionavel` e `Movel` são interfaces pequenas e específicas.
- **DIP:** `JogoService` recebe `RankingRepository`, enquanto `Main` escolhe
  a implementação concreta `RankingService`.

## Funcionalidades

O menu oferece:

```text
1. Iniciar Nova Missão
2. Visualizar Ranking Top 5
3. Resetar Ranking
4. Sair
```

Durante a missão:

- `w`, `s`, `a` e `d` movem a nave dentro dos limites do mapa;
- `c` embarca um passageiro na posição atual;
- `q` aborta a missão e retorna ao menu;
- colisões reduzem as vidas da nave;
- movimentos reduzem um ponto;
- o embarque adiciona a pontuação do tipo de passageiro;
- a vitória exige resgatar todos os passageiros e retornar a `(0,0)`;
- a tela mostra pontos, vidas, passageiros a bordo e passageiros restantes.

As pontuações iniciais são 30 no modo fácil, 20 no médio e 15 no difícil.
Professor vale 15 pontos, Engenheiro vale 20 e Astronauta vale 10.

## Alterações visuais

O renderer usa os símbolos definidos pelas entidades do domínio para tornar o
mapa mais legível:

```text
🚀 Nave       🔳 Plataforma       🎓 Professor
🔧 Engenheiro 🔭 Astronauta       ☄️ Asteroide
👾 Inimigo    . Vazio
```

Essa mudança é apenas de apresentação e não altera as regras da missão.

## Validação do tamanho do mapa

O serviço calcula o tamanho mínimo necessário para cada dificuldade. Se o
jogador informar um mapa pequeno demais, o programa informa o valor mínimo e
continua a partida com uma área suficiente para posicionar passageiros e
perigos. Assim, a geração aleatória não encerra a aplicação com
`IllegalStateException`.

## Ranking

O `Main` usa o arquivo `ranking-solid-exercicio10.json`. Apesar da extensão
herdada do nome definido no tutorial, o formato atual é texto delimitado por
`|`, com nome, pontuação, dificuldade, passageiros coletados, data e tempo de
missão. O arquivo é criado na raiz do projeto após uma vitória.

O ranking é ordenado pela pontuação, exibido no menu como Top 5 e pode ser
apagado pela opção de reset. Ao fim de uma vitória, as estatísticas mostram o
recorde atual a ser batido (ou avisam que um novo recorde foi estabelecido) e
parabenizam o piloto que entrou no Top 5, como no jogo original. Linhas
malformadas no arquivo são ignoradas na leitura.

## Compilação e execução

Execute os comandos a partir da raiz do projeto.

### Script recomendado no Windows

```cmd
run start
```

O script configura UTF-8, compila todos os arquivos de `solidexercicio10` e
inicia `solidexercicio10.Main`.

### PowerShell

```powershell
New-Item -ItemType Directory -Force -Path out | Out-Null
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src\solidexercicio10 | ForEach-Object FullName)
java '-Dfile.encoding=UTF-8' -cp out solidexercicio10.Main
```

### CMD

```cmd
if not exist out mkdir out
chcp 65001
javac -encoding UTF-8 -d out src\solidexercicio10\*.java src\solidexercicio10\model\*.java src\solidexercicio10\presentation\*.java src\solidexercicio10\repository\*.java src\solidexercicio10\service\*.java
java -Dfile.encoding=UTF-8 -cp out solidexercicio10.Main
```

## Testes manuais recomendados

1. Iniciar o jogo e verificar o menu.
2. Iniciar uma missão nos três níveis de dificuldade.
3. Informar um mapa pequeno e confirmar o ajuste automático.
4. Movimentar a nave, embarcar passageiros e retornar à origem.
5. Confirmar as estatísticas, o recorde e o salvamento do ranking após a
   vitória.
6. Consultar e resetar o ranking.
7. Abortar uma missão com `q` e confirmar o retorno imediato ao menu, sem
   nenhuma colisão depois da mensagem de aborto.
8. Digitar um comando inválido e confirmar que os inimigos não se movem.

O teste automatizado dos limites dos inimigos fica em
`test/solidexercicio10/TesteLimitesInimigos.java`; os comandos estão no
`README.md` da raiz.

Para a revisão consolidada da atividade, consulte `REVISAO-SOLID.md` na raiz
do projeto. A versão original continua disponível em `src/exercicio10`.
