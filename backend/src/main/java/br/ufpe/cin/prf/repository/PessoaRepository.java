package br.ufpe.cin.prf.repository;

import br.ufpe.cin.prf.entity.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PessoaRepository extends JpaRepository<Pessoa, String> {

    List<Pessoa> findAllByOrderByNomeCompletoAsc();

    /**
     * Corrige o CPF de uma pessoa.
     *
     * O JPA não permite alterar a chave primária de uma entidade, então usamos
     * SQL nativo. Todas as FKs do esquema têm ON UPDATE CASCADE: o banco propaga
     * o novo CPF para a subclasse, contatos, medicamentos, avaliações e consultas.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE pessoa SET cpf = :novoCpf WHERE cpf = :cpfAtual", nativeQuery = true)
    int atualizarCpf(@Param("cpfAtual") String cpfAtual, @Param("novoCpf") String novoCpf);
}
