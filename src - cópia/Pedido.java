import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Pedido {

	/** Quantidade máxima de produtos de um pedido */
	private static final int MAX_PRODUTOS = 10;
	
	/** Porcentagem de desconto para pagamentos à vista */
	private static final double DESCONTO_PG_A_VISTA = 0.15;
	
	/** Vetor para armazenar os itens do pedido */
	private ItemDePedido[] itens;
	
	/** Data de criação do pedido */
	private LocalDate dataPedido;
	
	/** Indica a quantidade total de itens no pedido até o momento */
	private int quantProdutos = 0;
	
	/** Indica a forma de pagamento do pedido sendo: 1, pagamento à vista; 2, pagamento parcelado */
	private int formaDePagamento;
	
	/** Construtor do pedido.
	 *  Cria o vetor de itens, armazena a data e a forma de pagamento.
	 */  
	public Pedido(LocalDate dataPedido, int formaDePagamento) {
		itens = new ItemDePedido[MAX_PRODUTOS];
		quantProdutos = 0;
		this.dataPedido = dataPedido;
		this.formaDePagamento = formaDePagamento;
	}
	
	/**
     * Tenta incluir um produto neste pedido.
     * Se o produto já existir no pedido, exibe "Produto já inserido." e retorna false.
     * Valida o estoque antes de incluir. Ao incluir, baixa o estoque do produto.
     * @param novo O produto a ser incluído no pedido
     * @param quantidade A quantidade desejada
     * @return true se incluiu com sucesso, false caso contrário.
     */
	public boolean incluirProduto(Produto novo, int quantidade) {
		// Cria um item temporário só para usar o equals
		ItemDePedido novoItem = new ItemDePedido(novo, quantidade, novo.valorDeVenda());

		// Verifica se o produto já está no pedido
		for (int i = 0; i < quantProdutos; i++) {
			if (itens[i].equals(novoItem)) {
				System.out.println("Produto já inserido.");
				return false;
			}
		}

		// Valida estoque
		if (novo.getQuantidadeEmEstoque() < quantidade) {
			System.out.println("Estoque insuficiente. Disponível: " + novo.getQuantidadeEmEstoque());
			return false;
		}

		// Verifica espaço no vetor
		if (quantProdutos >= MAX_PRODUTOS) {
			System.out.println("Pedido cheio.");
			return false;
		}

		// Baixa estoque e inclui o item
		novo.reduzirEstoque(quantidade);
		itens[quantProdutos++] = novoItem;
		return true;
	}

	/**
	 * Altera a quantidade de um item já existente no pedido.
	 * Valida o estoque considerando a diferença em relação à quantidade atual.
	 * @param produto O produto cujo item será alterado
	 * @param novaQuantidade A nova quantidade total desejada
	 * @return true se alterou com sucesso, false caso contrário.
	 */
	public boolean alterarQuantidadeItem(Produto produto, int novaQuantidade) {
		for (int i = 0; i < quantProdutos; i++) {
			if (itens[i].getProduto().equals(produto)) {
				int quantidadeAtual = itens[i].getQuantidade();
				int diferenca = novaQuantidade - quantidadeAtual;

				if (diferenca > 0 && produto.getQuantidadeEmEstoque() < diferenca) {
					System.out.println("Estoque insuficiente. Disponível: " + produto.getQuantidadeEmEstoque());
					return false;
				}

				// Ajusta o estoque: se aumentou retira; se diminuiu devolve
				if (diferenca > 0) {
					produto.reduzirEstoque(diferenca);
				} else {
					produto.setQuantidadeEmEstoque(produto.getQuantidadeEmEstoque() + Math.abs(diferenca));
				}

				itens[i].setQuantidade(novaQuantidade);
				return true;
			}
		}
		return false;
	}

	public ItemDePedido[] getItens() {
		return itens;
	}

	public int getQuantItens() {
		return quantProdutos;
	}
	
	/**
     * Calcula e retorna o valor final do pedido (soma dos subtotais de cada item).
     * Caso a forma de pagamento seja à vista, aplica o desconto correspondente.
     * @return Valor final do pedido (double)
     */
	public double valorFinal() {
		double valorPedido = 0;
		
		for (int i = 0; i < quantProdutos; i++) {
			valorPedido += itens[i].calcularSubtotal();
		}
		
		if (formaDePagamento == 1) {
			valorPedido = valorPedido * (1.0 - DESCONTO_PG_A_VISTA);
		}
		return valorPedido;
	}
	
	/**
     * Representação, em String, do pedido.
     * Contém um cabeçalho com sua data e o número de itens no pedido.
     * Depois, em cada linha, a descrição de cada item do pedido.
     * Ao final, mostra a forma de pagamento, o percentual de desconto (se for o caso) e o valor a ser pago.
     */
	@Override
	public String toString() {
		StringBuilder stringPedido = new StringBuilder();
		DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		
		stringPedido.append("Data do pedido: " + formatoData.format(dataPedido) + "\n");
		stringPedido.append("Pedido com " + quantProdutos + " produto(s).\n");
		stringPedido.append("Produtos no pedido:\n");
		for (int i = 0; i < quantProdutos; i++) {
			stringPedido.append(itens[i].getProduto().toString()
				+ " | Qtd: " + itens[i].getQuantidade()
				+ " | Subtotal: R$ " + String.format("%.2f", itens[i].calcularSubtotal()) + "\n");
		}
		
		stringPedido.append("Pedido pago ");
		if (formaDePagamento == 1) {
			stringPedido.append("à vista. Percentual de desconto: " + String.format("%.2f", DESCONTO_PG_A_VISTA * 100) + "%\n");
		} else {
			stringPedido.append("parcelado.\n");
		}
		
		stringPedido.append("Valor total do pedido: R$ " + String.format("%.2f", valorFinal()));
		
		return stringPedido.toString();
	}
	
	/**
     * Igualdade de pedidos: caso possuam a mesma data. 
     * @param obj Outro pedido a ser comparado 
     * @return booleano true/false conforme o parâmetro possua a data igual ou não a este pedido.
     */
    @Override
    public boolean equals(Object obj) {
        Pedido outro = (Pedido)obj;
        return this.dataPedido.equals(outro.dataPedido);
    }
}