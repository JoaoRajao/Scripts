import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ProdutoPerecivel extends Produto {

    private static final double DESCONTO = 0.25;
    private static final int PRAZO_DESCONTO_DIAS = 7;

    private final LocalDate dataDeValidade;

    public ProdutoPerecivel(String desc, double precoCusto, double margemLucro, LocalDate dataDeValidade) {
        super(desc, precoCusto, margemLucro);
        if (dataDeValidade == null) {
            throw new IllegalArgumentException("Data de validade inválida.");
        }
        if (dataDeValidade.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Produto já fora da data de validade.");
        }
        this.dataDeValidade = dataDeValidade;
    }

    @Override
    public double valorDeVenda() {
        LocalDate hoje = LocalDate.now();
        if (hoje.isAfter(dataDeValidade)) {
            throw new IllegalStateException("Valor de venda não pode ser consultado após a validade.");
        }

        double valorBase = super.valorDeVenda();
        long diasAteVencimento = ChronoUnit.DAYS.between(hoje, dataDeValidade);

        if (diasAteVencimento <= PRAZO_DESCONTO_DIAS) {
            return valorBase * (1 - DESCONTO);
        }

        return valorBase;
    }

    /**
     * Gera uma linha de texto a partir dos dados do produto. Preço e margem de lucro vão formatados com 2 casas decimais.
     * Data de validade vai no formato dd/mm/aaaa
     * @return Uma string no formato "2;descrição;preçoDeCusto;margemDeLucro;dataDeValidade"
     */
    @Override
    public String gerarDadosTexto() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return String.format("2;%s;%.2f;%.2f;%s;%d",
                getDescricao(), precoCusto, margemLucro, dataDeValidade.format(formatter), getQuantidadeEmEstoque());
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
