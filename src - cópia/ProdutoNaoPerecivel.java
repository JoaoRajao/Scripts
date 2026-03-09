public class ProdutoNaoPerecivel extends Produto {

    public ProdutoNaoPerecivel(String desc, double precoCusto, double margemLucro) {
        super(desc, precoCusto, margemLucro);
    }

    public ProdutoNaoPerecivel(String desc, double precoCusto) {
        super(desc, precoCusto);
    }

    @Override
    public double valorDeVenda() {
        return (precoCusto * (1.0 + margemLucro));
    }

    /**
     * Gera uma linha de texto a partir dos dados do produto. Preço e margem de lucro vão formatados com 2 casas decimais.
     * @return Uma string no formato "1;descrição;preçoDeCusto;margemDeLucro"
     */
    @Override
    public String gerarDadosTexto() {
        return String.format("1;%s;%.2f;%.2f;%d", getDescricao(), precoCusto, margemLucro, getQuantidadeEmEstoque());
    }
}
