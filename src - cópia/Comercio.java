import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Comercio {
    /** Para inclusão de novos produtos no vetor */
    static final int MAX_NOVOS_PRODUTOS = 10;

    /** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;

    /** Scanner para leitura do teclado */
    static Scanner teclado;

    /** Vetor de produtos cadastrados. Sempre terá espaço para 10 novos produtos a cada execução */
    static Produto[] produtosCadastrados;

    /** Quantidade produtos cadastrados atualmente no vetor */
    static int quantosProdutos;

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa(){
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho(){
        System.out.println("AEDII COMÉRCIO DE COISINHAS");
        System.out.println("===========================");
    }

    /** Imprime o menu principal, lê a opção do usuário e a retorna (int). */
    static int menu(){
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar e listar um produto");
        System.out.println("3 - Cadastrar novo produto");
        System.out.println("4 - Criar novo pedido");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        return Integer.parseInt(teclado.nextLine());
    }

    /**
     * Lê os dados de um arquivo texto e retorna um vetor de produtos.
     */
    static Produto[] lerProdutos(String nomeArquivoDados) {
        Produto[] vetorProdutos;
        try {
            File arquivo = new File(nomeArquivoDados);
            Scanner leitor = new Scanner(arquivo, "ISO-8859-2");

            int quantidade = Integer.parseInt(leitor.nextLine().trim());
            vetorProdutos = new Produto[quantidade + MAX_NOVOS_PRODUTOS];
            quantosProdutos = 0;

            for (int i = 0; i < quantidade && leitor.hasNextLine(); i++) {
                String linha = leitor.nextLine().trim();
                if (!linha.isEmpty()) {
                    Produto p = Produto.criarDoTexto(linha);
                    if (p != null) {
                        vetorProdutos[quantosProdutos++] = p;
                    }
                }
            }
            leitor.close();
        } catch (FileNotFoundException e) {
            System.out.println("Arquivo não encontrado. Iniciando com cadastro vazio.");
            vetorProdutos = new Produto[MAX_NOVOS_PRODUTOS];
            quantosProdutos = 0;
        } catch (Exception e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
            vetorProdutos = new Produto[MAX_NOVOS_PRODUTOS];
            quantosProdutos = 0;
        }
        return vetorProdutos;
    }

    /** Lista todos os produtos cadastrados, numerados, com estoque. */
    static void listarTodosOsProdutos(){
        cabecalho();
        System.out.println("\nPRODUTOS CADASTRADOS:");
        if (quantosProdutos == 0) {
            System.out.println("Nenhum produto cadastrado.");
            return;
        }
        for (int i = 0; i < quantosProdutos; i++) {
            if (produtosCadastrados[i] != null)
                System.out.println(String.format("%02d - %s [Estoque: %d]",
                        (i + 1),
                        produtosCadastrados[i].toString(),
                        produtosCadastrados[i].getQuantidadeEmEstoque()));
        }
    }

    /** Localiza um produto pelo nome e imprime seus dados. */
    static void localizarProdutos(){
        cabecalho();
        System.out.print("Digite o nome do produto a localizar: ");
        String nomeBusca = teclado.nextLine();

        Produto chave;
        try {
            chave = new ProdutoNaoPerecivel(nomeBusca, 0.01);
        } catch (Exception e) {
            System.out.println("Nome muito curto para pesquisa (mínimo 3 caracteres).");
            return;
        }

        boolean encontrado = false;
        for (int i = 0; i < quantosProdutos; i++) {
            if (produtosCadastrados[i] != null && produtosCadastrados[i].equals(chave)) {
                System.out.println("Produto encontrado:");
                System.out.println(produtosCadastrados[i].toString());
                encontrado = true;
            }
        }
        if (!encontrado) {
            System.out.println("Produto \"" + nomeBusca + "\" não encontrado.");
        }
    }

    /** Rotina de cadastro de um novo produto. */
    static void cadastrarProduto(){
        cabecalho();
        System.out.println("CADASTRO DE NOVO PRODUTO");
        System.out.println("1 - Produto Não Perecível");
        System.out.println("2 - Produto Perecível");
        System.out.print("Tipo do produto: ");
        int tipo;
        try {
            tipo = Integer.parseInt(teclado.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Tipo inválido.");
            return;
        }

        System.out.print("Descrição: ");
        String desc = teclado.nextLine().trim();

        System.out.print("Preço de custo (ex: 2.50): ");
        double precoCusto;
        try {
            precoCusto = Double.parseDouble(teclado.nextLine().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("Preço inválido.");
            return;
        }

        System.out.print("Margem de lucro (ex: 0.30 para 30%): ");
        double margemLucro;
        try {
            margemLucro = Double.parseDouble(teclado.nextLine().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("Margem inválida.");
            return;
        }

        System.out.print("Quantidade em estoque: ");
        int estoque;
        try {
            estoque = Integer.parseInt(teclado.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Quantidade inválida.");
            return;
        }

        Produto novoProduto = null;
        try {
            if (tipo == 1) {
                novoProduto = new ProdutoNaoPerecivel(desc, precoCusto, margemLucro);
            } else if (tipo == 2) {
                System.out.print("Data de validade (dd/mm/aaaa): ");
                String dataStr = teclado.nextLine().trim();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate dataDeValidade = LocalDate.parse(dataStr, formatter);
                novoProduto = new ProdutoPerecivel(desc, precoCusto, margemLucro, dataDeValidade);
            } else {
                System.out.println("Tipo de produto inválido.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Erro ao criar produto: " + e.getMessage());
            return;
        }

        novoProduto.setQuantidadeEmEstoque(estoque);

        if (quantosProdutos >= produtosCadastrados.length) {
            System.out.println("Capacidade máxima do vetor atingida. Produto não cadastrado.");
            return;
        }

        produtosCadastrados[quantosProdutos++] = novoProduto;
        System.out.println("Produto cadastrado com sucesso!");
        System.out.println(novoProduto.toString());
    }

    /**
     * Fluxo de criação de um novo pedido.
     * O usuário escolhe os produtos pelo número, informa a quantidade,
     * e o sistema gerencia estoque e duplicatas.
     */
    static void criarPedido(){
        cabecalho();
        System.out.println("NOVO PEDIDO");
        System.out.println("Forma de pagamento: 1 - À vista  |  2 - Crédito");
        System.out.print("Escolha: ");
        int forma;
        try {
            forma = Integer.parseInt(teclado.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Opção inválida.");
            return;
        }

        Pedido pedido = new Pedido(LocalDate.now(), forma);

        String resp = "s";
        while (resp.equalsIgnoreCase("s")) {
            listarTodosOsProdutos();
            System.out.print("\nNúmero do produto a adicionar (0 para encerrar): ");
            int numProduto;
            try {
                numProduto = Integer.parseInt(teclado.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Número inválido.");
                continue;
            }

            if (numProduto == 0) break;
            if (numProduto < 1 || numProduto > quantosProdutos) {
                System.out.println("Produto inválido.");
                continue;
            }

            Produto escolhido = produtosCadastrados[numProduto - 1];

            System.out.print("Quantidade: ");
            int qtd;
            try {
                qtd = Integer.parseInt(teclado.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Quantidade inválida.");
                continue;
            }

            boolean incluido = pedido.incluirProduto(escolhido, qtd);

            // Se retornou false e a mensagem foi "Produto já inserido", pergunta se quer alterar
            if (!incluido) {
                // Verifica se o produto já está no pedido
                boolean jaNoPedido = false;
                for (int i = 0; i < pedido.getQuantItens(); i++) {
                    if (pedido.getItens()[i].getProduto().equals(escolhido)) {
                        jaNoPedido = true;
                        break;
                    }
                }
                if (jaNoPedido) {
                    System.out.print("Deseja alterar a quantidade deste item? (s/n): ");
                    String alterar = teclado.nextLine().trim();
                    if (alterar.equalsIgnoreCase("s")) {
                        System.out.print("Nova quantidade total: ");
                        int novaQtd;
                        try {
                            novaQtd = Integer.parseInt(teclado.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Quantidade inválida.");
                            continue;
                        }
                        boolean ok = pedido.alterarQuantidadeItem(escolhido, novaQtd);
                        if (ok) System.out.println("Quantidade atualizada.");
                    }
                }
            } else {
                System.out.println("Produto adicionado ao pedido.");
            }

            System.out.print("Adicionar outro produto? (s/n): ");
            resp = teclado.nextLine().trim();
        }

        System.out.println("\n" + pedido.toString());
    }

    /**
     * Salva os dados dos produtos cadastrados no arquivo csv.
     */
    public static void salvarProdutos(String nomeArquivo){
        try {
            FileWriter escritor = new FileWriter(nomeArquivo, false);
            escritor.write(quantosProdutos + "\n");
            for (int i = 0; i < quantosProdutos; i++) {
                if (produtosCadastrados[i] != null) {
                    escritor.write(produtosCadastrados[i].gerarDadosTexto() + "\n");
                }
            }
            escritor.close();
            System.out.println("Dados salvos em \"" + nomeArquivo + "\".");
        } catch (IOException e) {
            System.out.println("Erro ao salvar o arquivo: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        teclado = new Scanner(System.in, Charset.forName("ISO-8859-2"));
        nomeArquivoDados = "dadosProdutos.csv";
        produtosCadastrados = lerProdutos(nomeArquivoDados);
        int opcao = -1;
        do {
            opcao = menu();
            switch (opcao) {
                case 1 -> listarTodosOsProdutos();
                case 2 -> localizarProdutos();
                case 3 -> cadastrarProduto();
                case 4 -> criarPedido();
            }
            if (opcao != 0) pausa();
        } while (opcao != 0);

        salvarProdutos(nomeArquivoDados);
        teclado.close();
    }
}
